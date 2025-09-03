package co.edu.escuelaing.arep.reflexionlab;

import co.edu.escuelaing.arep.reflexionlab.Anotations.GetMapping;
import co.edu.escuelaing.arep.reflexionlab.Anotations.RequestParam;
import co.edu.escuelaing.arep.reflexionlab.Anotations.RestController;
import co.edu.escuelaing.arep.reflexionlab.util.HtmlFetcher;
import co.edu.escuelaing.arep.reflexionlab.util.Request;
import co.edu.escuelaing.arep.reflexionlab.util.Response;
import co.edu.escuelaing.arep.reflexionlab.util.Service;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Framework {
    private static final Map<String, Service> services = new HashMap<>();
    private static HtmlFetcher htmlFetcher = new HtmlFetcher();

    private static Object controllerInstance;
    private static final int THREAD_POOL_SIZE = 10;
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);



    public static void start() throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started at port " + port);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            executor.submit(() -> handle(clientSocket));
        }
    }

    public static void loadComponents() {
        try {
            Class<?> controllerClass = Class.forName("co.edu.escuelaing.arep.reflexionlab.controller.Controller");

            if (controllerClass.isAnnotationPresent(RestController.class)) {
                controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

                for (Method method : controllerClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GetMapping.class)) {
                        GetMapping getMapping = method.getAnnotation(GetMapping.class);
                        String path = getMapping.value();

                        Service service = (request, response) -> {
                            try {
                                Object[] params = new Object[method.getParameterCount()];
                                var methodParams = method.getParameters();

                                for (int i = 0; i < methodParams.length; i++) {
                                    var param = methodParams[i];

                                    if (param.isAnnotationPresent(RequestParam.class)) {
                                        RequestParam annotation = param.getAnnotation(RequestParam.class);
                                        String key = annotation.value();
                                        String defaultValue = annotation.defaultValue();

                                        System.out.println("metodo " + method.getName());
                                        System.out.println("value " + param.getName());
                                        System.out.println("default " + defaultValue);



                                        String value = request.getQueryParam(key);

                                        if (value == null || value.isEmpty()) {
                                            value = defaultValue;
                                        }

                                        params[i] = value;
                                    } else {
                                        params[i] = null;
                                    }
                                }

                                Object result = method.invoke(controllerInstance, params);
                                return result != null ? result.toString() : "";
                            } catch (Exception e) {
                                e.printStackTrace();
                                response.setStatusCode(500);
                                return "Internal Server Error";
                            }
                        };
                        services.put(path, service);

                        System.out.println("Ruta cargada: " + path + " -> " + method.getName());
                    }
                }
            } else {
                System.out.println("La clase no tiene la anotación @RestController");
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Clase no encontrada: ", e);
        } catch (Exception e) {
            throw new RuntimeException("Error cargando componentes", e);
        }
    }

    private static void handle(Socket client) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                OutputStream out = client.getOutputStream()
        ) {
            String requestLine = in.readLine();
            if (requestLine == null || !requestLine.startsWith("GET ")) {
                client.close();
                return;
            }

            String fullPath = requestLine.split(" ")[1];
            String path = fullPath.split("\\?")[0];

            String query = fullPath.contains("?") ? fullPath.split("\\?", 2)[1] : null;

            String queryString = null;
            if (fullPath.contains("?")) {
                queryString = fullPath.split("\\?", 2)[1];
            }


            Request req = Request.builder()
                    .method("GET")
                    .path(path)
                    .query(queryString)
                    .build();

            Service svc = services.get(path);

            if (svc != null) {
                String body = svc.handle(req, Response.builder().statusCode(200).build());
                Response response = Response.builder()
                        .statusCode(200)
                        .headers(Map.of("Content-Type", "text/plain"))
                        .body(body)
                        .build();

                String rawResponse = generateResponse(response);
                out.write(rawResponse.getBytes());
            } else {
                try {
                    String filePath = path.equals("/") ? "index.html" : path.substring(1);
                    byte[] fileBytes = HtmlFetcher.readFile(filePath);
                    String mime = HtmlFetcher.getMimeType(filePath);

                    PrintWriter writer = new PrintWriter(out, false);
                    writer.print("HTTP/1.1 200 OK\r\n");
                    writer.print("Content-Type: " + mime + "\r\n");
                    writer.print("Content-Length: " + fileBytes.length + "\r\n");
                    writer.print("Connection: close\r\n");
                    writer.print("\r\n");
                    writer.flush();

                    out.write(fileBytes);
                } catch (IOException e) {
                    String errorMessage = "404 Not Found";
                    String rawResponse = "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "Content-Length: " + errorMessage.length() + "\r\n" +
                            "\r\n" +
                            errorMessage;
                    out.write(rawResponse.getBytes());
                }
            }

            out.flush();
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String generateResponse(Response response) {
        StringBuilder http = new StringBuilder();
        String statusText = getStatusText(response.getStatusCode());
        http.append("HTTP/1.1 ").append(response.getStatusCode()).append(" ").append(statusText).append("\r\n");
        response.getHeaders().forEach((k, v) -> http.append(k).append(": ").append(v).append("\r\n"));
        String body = response.getBody() == null ? "" : response.getBody();
        int length = body.getBytes().length;
        http.append("Content-Length: ").append(length).append("\r\n");
        http.append("\r\n").append(body);
        return http.toString();
    }

    private static String getStatusText(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "";
        };
    }

}

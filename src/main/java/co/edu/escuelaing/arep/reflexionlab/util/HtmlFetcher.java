package co.edu.escuelaing.arep.reflexionlab.util;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HtmlFetcher {
    private static String basePath;

    public void setHtmlFetcher(String basePath) {
        this.basePath = basePath;
    }

    public static byte[] readFile(String filePath) throws IOException {
        if (filePath == null || filePath.equals("/") || filePath.isEmpty()) {
            filePath = "index.html";
        }

        return Files.readAllBytes(Paths.get(basePath + filePath));
    }

    public static String getMimeType(String filePath) {
        if (filePath.endsWith(".html")) return "text/html";
        if (filePath.endsWith(".css")) return "text/css";
        if (filePath.endsWith(".js")) return "application/javascript";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
        if (filePath.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
}
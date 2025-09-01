package co.edu.escuelaing.arep.reflexionlab.util;

import lombok.Builder;
import lombok.Data;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Data
@Builder
public class Request {
    private String method;
    private String path;
    private Map<String, String> headers;
    private String query;
    private String body;

    public String getValue() {
        return query != null ? URLDecoder.decode(query, StandardCharsets.UTF_8) : null;
    }

    public String getQueryParam(String key) {
        if (query == null) return null;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }


}
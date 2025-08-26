package co.edu.escuelaing.arep.reflexionlab.util;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
@Data
@Builder(toBuilder = true)
public class Response {
    private int statusCode;
    private Map<String, String> headers;
    private String body;
    private byte[] rawBody;
}
package org.example.httpserver;

import java.util.HashMap;
import java.util.Map;

public class ServerRequest {

    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;

    public ServerRequest(String method, String path, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public static ServerRequest parse(String requestData) {
        String[] lines = requestData.split("\r\n");
        String[] requestLine = lines[0].split(" ");
        String method = requestLine[0];
        String path = requestLine[1];

        Map<String, String> headers = new HashMap<>();
        int i = 1;
        while (i < lines.length && !lines[i].isEmpty()) {
            String[] header = lines[i].split(": ");
            if (header.length == 2) {
                headers.put(header[0], header[1]);
            }
            i++;
        }

        StringBuilder bodyBuilder = new StringBuilder();
        for (int j = i + 1; j < lines.length; j++) {
            bodyBuilder.append(lines[j]).append("\r\n");
        }
        String body = bodyBuilder.toString().trim();

        return new ServerRequest(method, path, headers, body);
    }


}
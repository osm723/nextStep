package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HTTPRequestParser {
    private String method;
    private String url;
    private Map<String, String> headers = new HashMap<>();
    private StringBuilder body = new StringBuilder();

    public HTTPRequestParser(InputStream inputStream) throws IOException {
        parseRequest(inputStream);
    }

    private void parseRequest(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // Parse request line
        String line = reader.readLine();
        if (line == null || line.isEmpty()) {
            throw new IOException("Empty request line");
        }

        String[] requestLineParts = line.split(" ");
        if (requestLineParts.length < 3) {
            throw new IOException("Invalid request line: " + line);
        }

        method = requestLineParts[0];
        url = requestLineParts[1];

        // Parse headers
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] headerParts = line.split(":", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0].trim(), headerParts[1].trim());
            }
        }

        // Parse body (if present)
        while (reader.ready()) {
            line = reader.readLine();
            body.append(line).append("\n");
        }
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body.toString().trim();
    }
}
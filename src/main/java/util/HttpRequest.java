package util;

import model.HttpMethod;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private String method;
    private String path;

    private boolean logined;

    private int contentLength;

    private Map<String, String> headers = new HashMap<>();

    private Map<String, String> params = new HashMap<>();

    private RequestLine requestLine;


    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String line = br.readLine();
            if (line == null) {
                return;
            }

            //processRequestLine(line);
            requestLine = new RequestLine();
            requestLine.requestLine(line);

            line = br.readLine();
            while (!line.equals("")) {
                String[] tokens =  line.split(" ");
                headers.put(tokens[0].trim(), tokens[1].trim());
                line = br.readLine();

                if (line.contains("Cookie")) {
                    logined = isLogin(line);
                }

                if (line.contains("Content-Length")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }

            if ("POST".equals(method)) {
                String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(body);
            } else {
                params = requestLine.getParams();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void processRequestLine(String requestLine) {

        String[] tokens =  requestLine.split(" ");
        method = tokens[0];

        if ("POST".equals(method)) {
            path = tokens[1];
            return;
        }

        int index = tokens[1].indexOf("?");
        if (index == -1) {
            path = tokens[1];
        } else {
            path = tokens[1].substring(0, index);
            params = HttpRequestUtils.parseQueryString(tokens[1].substring(index+1));
        }
    }

    private static boolean isLogin(String line) {
        String[] tokens = line.split(":");
        Map<String, String> cookiesMap = HttpRequestUtils.parseCookies(tokens[1].trim());
        String value = cookiesMap.get("logined");

        if (value == null) {
            return false;
        }

        return Boolean.parseBoolean(value);
    }

    public HttpMethod getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeaders(String name) {
        return headers.get(name);
    }

    public String getParams(String name) {
        return params.get(name);
    }

    public boolean isLogined() {
        return logined;
    }

    public int getContentLength() {
        return contentLength;
    }
}

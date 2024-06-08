package util;

import model.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class RequestLine {

    private HttpMethod method;
    private String path;

    private Map<String, String> params = new HashMap<>();

    public void requestLine(String requestLine) {

        String[] tokens =  requestLine.split(" ");
        //method = tokens[0];

        method = HttpMethod.valueOf(tokens[0]);
        if (method == HttpMethod.POST) {
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

//        String[] tokens =  requestLine.split(" ");
//        if (tokens.length != 3) {
//            throw new Exception();
//        }
//
//        method = tokens[0];
//
//        if ("POST".equals(method)) {
//            path = tokens[1];
//            return;
//        }
//
//        int index = tokens[1].indexOf("?");
//        if (index == -1) {
//            path = tokens[1];
//        } else {
//            path = tokens[1].substring(0, index);
//            params = HttpRequestUtils.parseQueryString(tokens[1].substring(index+1));
//        }
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParams() {
        return params;
    }
}

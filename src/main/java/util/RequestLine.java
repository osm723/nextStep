package util;

import Controller.ListUserController;
import model.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RequestLine {
    private static final Logger log = LoggerFactory.getLogger(RequestLine.class);

    private HttpMethod method;

    private String path;

    private String queryString;


    public RequestLine(String requestLine) {
        String[] tokens = requestLine.split(" ");
        this.method = HttpMethod.valueOf(tokens[0]);

        String[] url = tokens[1].split("\\?");
        this.path = url[0];

        if (url.length == 2) {
            this.queryString = url[1];
        }
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQueryString() {
        return queryString;
    }
}

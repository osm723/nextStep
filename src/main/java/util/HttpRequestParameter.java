package util;

public class HttpRequestParameter {

    private String method;
    private String url;

    private StringBuilder body;

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public StringBuilder getBody() {
        return body;
    }

    public void setBody(StringBuilder body) {
        this.body = body;
    }
}

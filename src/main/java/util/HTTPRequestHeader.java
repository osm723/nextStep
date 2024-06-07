package util;

import java.util.HashMap;
import java.util.Map;

public class HTTPRequestHeader {

    private Map<String, String> headers = new HashMap<>();

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

}

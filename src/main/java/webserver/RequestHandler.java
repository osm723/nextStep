package webserver;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

import Controller.Controller;
import Controller.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            String path = getDefaultPath(request.getPath());

//            if (getSessionId(request.getHeader("Cookie")) == null) {
//                response.addHeader("Set-Cookie", "JSESSIONID="+ UUID.randomUUID());
//            }

            if (request.getCookies().getCookie("JSESSIONID") == null) {
                response.addHeader("Set-Cookie", "JSESSIONID="+ UUID.randomUUID());
            }

            log.info("path=====================", path);

            Controller controller = RequestMapping.getController(request.getPath());

            if (controller == null) {
                response.forward(path);
            } else {
                controller.service(request, response);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private String getSessionId(String cookieValue) {
        Map<String, String> cookies = HttpRequestUtils.parseCookies(cookieValue);
        return cookies.get("JSESSIONID");
    }

    private String getDefaultPath(String path) {
        if (path.equals("/")) {
            return "/index.html";
        }
        return path;
    }

}

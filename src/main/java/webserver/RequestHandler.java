package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    private StringBuilder body = new StringBuilder();

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

            if ("/user/create".startsWith(path)) {
                createUser(request, response);
            } else if ("/user/login".equals(path)) {
                login(request, response);
            } else if ("/user/list".equals(path)) {
                if (!isLogin(request.getHeaders("Cookie"))) {
                    response.sendRedirect("/user/login.html");
                    return;
                }
                listUser(response);
            } else {
                response.forward(path);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private static void listUser(HttpResponse response) {
        Collection<User> users = DataBase.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("<table boader='1'>");
        for (User user : users) {
            sb.append("<tr>");
            sb.append("<td>" + user.getUserId() + "</td>");
            sb.append("<td>" + user.getName() + "</td>");
            sb.append("<td>" + user.getEmail() + "</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");

        response.forwardBody(sb.toString());
    }

    private static void login(HttpRequest request, HttpResponse response) {
        User user = DataBase.findUserById(request.getParams("userId"));
        if (user != null) {
            if (user.getPassword().equals(request.getParams("password"))) {
                response.addHeader("Set-Cookie", "logined=true");
                response.sendRedirect("/index.html");
            } else {
                response.sendRedirect("/user/login_failed.html");
            }
        } else {
            response.sendRedirect("/user/login_failed.html");
        }
    }

    private static void createUser(HttpRequest request, HttpResponse response) {
        User user = new User(
                request.getParams("userId"),
                request.getParams("password"),
                request.getParams("name"),
                request.getParams("email")
        );
        DataBase.addUser(user);
        response.sendRedirect("/index.html");
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

    private String getDefaultPath(String path) {
        if (path.equals("/")) {
            return "/index.html";
        }
        return path;
    }

}

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
                User user = new User(
                        request.getParams("userId"),
                        request.getParams("password"),
                        request.getParams("name"),
                        request.getParams("email")
                );
                DataBase.addUser(user);
                response.sendRedirect("/index.html");
            } else if ("/user/login".equals(path)) {
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
            } else if ("/user/list".equals(path)) {
                if (!isLogin(request.getHeaders("Cookie"))) {
                    response.sendRedirect("/user/login.html");
                    return;
                }

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
            } else {
                response.forward(path);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /*
     * getReadBytesFile
     * Files 읽어서 urlPath return
     */
//    private static byte[] getReadBytesFile(String loginFailUrl) throws IOException {
//        return Files.readAllBytes(new File("./webapp" + loginFailUrl).toPath());
//    }

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

    /*
     * forward
     * 응답처리 = 직접응답
     */
//    private static void forward(OutputStream out, byte[] body, String contentType) {
//        DataOutputStream dos = new DataOutputStream(out);
//
//        try {
//            dos.writeBytes("HTTP/1.1 200 OK \r\n");
//            dos.writeBytes("Content-Type: "+contentType+"\r\n");
//            dos.writeBytes("Content-Length: " + body.length + "\r\n");
//            dos.writeBytes("\r\n");
//
//            // responseBody
//            dos.write(body, 0, body.length);
//            dos.flush();
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//    }

    /*
    * sendRedirect
    * 응답처리 = 리다이렉트
    */
//    private static void sendRedirect(OutputStream out, String url,boolean logined) {
//        DataOutputStream dos = new DataOutputStream(out);
//
//        try {
//            if (logined) {
//                //getResponse302LoginHeader
//                dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
//                dos.writeBytes("Set-Cookie: logined=true \r\n");
//                dos.writeBytes("Location: " + url + " \r\n");
//                dos.writeBytes("\r\n");
//            } else {
//                //response302Header
//                dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
//                dos.writeBytes("Location: " + url + " \r\n");
//                dos.writeBytes("\r\n");
//            }
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//    }

//    private static void getResponseResource(OutputStream out, String url) throws Exception{
//        DataOutputStream dos = new DataOutputStream(out);
//        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
//        //response200Header(dos, body.length);
//        forward(dos, body.length, "text/html;charset=utf-8");
//        responseBody(dos, body);
//    }

//    private static void getResponse302LoginHeader(DataOutputStream dos, String url) {
//        try {
//            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
//            dos.writeBytes("Set-Cookie: logined=true \r\n");
//            dos.writeBytes("Location: " + url + " \r\n");
//            dos.writeBytes("\r\n");
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//    }

//    private static void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
//        try {
//            dos.writeBytes("HTTP/1.1 200 OK \r\n");
//            dos.writeBytes("Content-Type: text/css\r\n");
//            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
//            dos.writeBytes("\r\n");
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//    }

//    private static void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
//        try {
//            dos.writeBytes("HTTP/1.1 200 OK \r\n");
//            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
//            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
//            dos.writeBytes("\r\n");
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//    }

//    private void response302Header(DataOutputStream dos, String url) {
//        try {
//            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
//            dos.writeBytes("Location: " + url + " \r\n");
//            dos.writeBytes("\r\n");
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//    }

//    private static void responseBody(DataOutputStream dos, byte[] body) {
//        try {
//            dos.write(body, 0, body.length);
//            dos.flush();
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//    }


}

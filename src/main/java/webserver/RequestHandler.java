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

            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String line = br.readLine();

            if (line == null) {
                return;
            }

            System.out.println("connection.getInetAddress()  = " + connection.getInetAddress());
            System.out.println("connection.getInputStream().toString()  = " + connection.getInputStream().toString());

            //String[] token =  line.split("GET /");
            String[] tokens =  line.split(" ");
            int contentLength = 0;
            boolean logined = false;

            while (!line.equals("")) {
                line = br.readLine();
                log.info("header======== {}", line);
                if (line.contains("Cookie")) {
                    logined = isLogin(line);
                }
                if (line.contains("Content-Length")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }

                String[] requestLineParts = line.split(" ");
//                if (requestLineParts.length < 3) {
//                    throw new IOException("Invalid request line: " + line);
//                }

                HttpRequestParameter parameters = new HttpRequestParameter();
                parameters.setMethod(requestLineParts[0]);
                parameters.setUrl(requestLineParts[1]);

                // Parse headers
                HTTPRequestHeader headers = new HTTPRequestHeader();
                while ((line = br.readLine()) != null && !line.isEmpty()) {
                    String[] headerParts = line.split(":", 2);
                    if (headerParts.length == 2) {
                        headers.setHeader(headerParts[0].trim(), headerParts[1].trim());
                    }
                }

                // Parse body (if present)
                while (br.ready()) {
                    line = br.readLine();
                    body.append(line).append("\n");
                }
                parameters.setBody(body);
            }

            String url = tokens[1];
            String loginFailUrl = "/user/login_failed.html";
            String loginUrl = "/user/login.html";

            if ("/user/create".startsWith(url)) {
                // GET
                //int index = url.indexOf("?");
                //String requestPath = url.substring(0,index);
                //String queryString = url.substring(index+1);
                //Map<String, String> paramMap = HttpRequestUtils.parseQueryString(queryString);

                String body = IOUtils.readData(br, contentLength);
                Map<String, String> paramMap = HttpRequestUtils.parseQueryString(body);

                log.info("params====== {}", paramMap.toString());

                User user = new User(paramMap.get("userId"), paramMap.get("password"), paramMap.get("name"), paramMap.get("email"));

                log.info("user====== {}", user);

                DataBase.addUser(user);

                sendRedirect(out, "/index.html", logined);
            } else if ("/user/login".equals(url)) {
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> paramMap = HttpRequestUtils.parseQueryString(body);

                User user = DataBase.findUserById(paramMap.get("userId"));
                if (user == null) {
                    forward(out, getReadBytesFile(loginFailUrl), "text/html;charset=utf-8");
                }

                if (user.getPassword().equals(paramMap.get("password"))) {
                    sendRedirect(out, "/index.html", logined);
                } else {
                    forward(out, getReadBytesFile(loginFailUrl), "text/html;charset=utf-8");
                }

            } else if ("/user/list".equals(url)) {
                if (!logined) {
                    forward(out, getReadBytesFile(loginUrl), "text/html;charset=utf-8");
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

                forward(out, sb.toString().getBytes(), "text/html;charset=utf-8");
            } else if (url.endsWith(".css")) {
                forward(out, getReadBytesFile(url), "text/css");
            } else {
                forward(out, getReadBytesFile(url), "text/html;charset=utf-8");
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /*
     * getReadBytesFile
     * Files 읽어서 urlPath return
     */
    private static byte[] getReadBytesFile(String loginFailUrl) throws IOException {
        return Files.readAllBytes(new File("./webapp" + loginFailUrl).toPath());
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

    /*
     * forward
     * 응답처리 = 직접응답
     */
    private static void forward(OutputStream out, byte[] body, String contentType) {
        DataOutputStream dos = new DataOutputStream(out);

        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: "+contentType+"\r\n");
            dos.writeBytes("Content-Length: " + body.length + "\r\n");
            dos.writeBytes("\r\n");

            // responseBody
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /*
    * sendRedirect
    * 응답처리 = 리다이렉트
    */
    private static void sendRedirect(OutputStream out, String url,boolean logined) {
        DataOutputStream dos = new DataOutputStream(out);

        try {
            if (logined) {
                //getResponse302LoginHeader
                dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
                dos.writeBytes("Set-Cookie: logined=true \r\n");
                dos.writeBytes("Location: " + url + " \r\n");
                dos.writeBytes("\r\n");
            } else {
                //response302Header
                dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
                dos.writeBytes("Location: " + url + " \r\n");
                dos.writeBytes("\r\n");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

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

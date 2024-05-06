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
import util.HttpRequestUtils;
import util.IOUtils;

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

            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String line = br.readLine();

            if (line == null) {
                return;
            }

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
            }

            String url = tokens[1];

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

                String returnUrl = "/index.html";

                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, returnUrl);
            } else if ("/user/login".equals(url)) {
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> paramMap = HttpRequestUtils.parseQueryString(body);

                User user = DataBase.findUserById(paramMap.get("userId"));
                if (user == null) {
                    getResponseResource(out, "/user/login_failed.html");
                }

                if (user.getPassword().equals(paramMap.get("password"))) {
                    DataOutputStream dos = new DataOutputStream(out);
                    getResponse302LoginHeader(dos, "/index.html");
                } else {
                    getResponseResource(out, "/user/login_failed.html");
                }

            } else if ("/user/list".equals(url)) {
                if (!logined) {
                    getResponseResource(out, "/user/login.html");
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

                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = sb.toString().getBytes();
                response200Header(dos, body.length);
                responseBody(dos, body);
            } else {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp/" + url).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
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

    private static void getResponseResource(OutputStream out, String url) throws Exception{
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private static void getResponse302LoginHeader(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: " + url + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + url + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}

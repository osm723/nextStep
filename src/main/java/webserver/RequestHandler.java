package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
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

            String[] token =  line.split("GET /");

            while (!line.equals("")) {
                line = br.readLine();
                log.info("header======== {}", line);
            }

            for (int i = 0; i < token.length; i++) {
                String filePath = token[i].replace(" HTTP/1.1","");
                if (!filePath.isEmpty()) {
                    log.info("filePath====== {}", filePath);

                    if (filePath.contains("user/create")) {

                        if (filePath.contains("POST")) {
                            String val = IOUtils.readData(br, 1000);
                            log.info("oh POST========== {}", val);
                            Map<String, String> map = HttpRequestUtils.parseQueryString(val);

                            log.info("oh userId========== {}", map.get("userId"));
                            log.info("oh password========== {}", map.get("password"));
                            log.info("oh name========== {}", map.get("name"));
                            log.info("oh email========== {}", map.get("email"));

                            User user = new User(map.get("userId"), map.get("password"), map.get("name"), map.get("email"));

                            DataBase.addUser(user);

                            filePath = "index.html";

                            byte[] body = Files.readAllBytes(new File("./webapp/" + filePath).toPath());

                            //
                            DataOutputStream dos = new DataOutputStream(out);
                            response302Header(dos, body.length);
                            responseBody(dos, body);

                            return;

                        } else {
                            int index = filePath.indexOf("?");
                            String requestPath = filePath.substring(0,index);
                            String parameter = filePath.substring(index+1);

                            Map<String, String> map = HttpRequestUtils.parseQueryString(parameter);
                            log.info("filePath2====== {}", map.toString());

                            User user = new User(map.get("userId"), map.get("password"), map.get("name"), map.get("email"));

                            DataBase.addUser(user);

                            filePath = "index.html";
                        }
                    }

                    byte[] body = Files.readAllBytes(new File("./webapp/" + filePath).toPath());

                    //
                    DataOutputStream dos = new DataOutputStream(out);
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                }
            }


            //DataOutputStream dos = new DataOutputStream(out);
            //byte[] body = "Hello World".getBytes();
            //response200Header(dos, body.length);
            //responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}

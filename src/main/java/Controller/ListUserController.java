package Controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpRequestUtils;
import util.HttpResponse;
import util.HttpSession;

import java.util.Collection;
import java.util.Map;

public class ListUserController implements Controller {

    private static final Logger log = LoggerFactory.getLogger(ListUserController.class);

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        //if (!isLogin(request.getHeader("Cookie"))) {
        if (!isLogin(request.getSession())) {
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
    }

    private static boolean isLogin(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return false;
        }
        return true;

//        String[] tokens = line.split(":");
//        Map<String, String> cookiesMap = HttpRequestUtils.parseCookies(tokens[1].trim());
//        String value = cookiesMap.get("logined");
//
//        if (value == null) {
//            return false;
//        }
//
//        return Boolean.parseBoolean(value);
    }
}

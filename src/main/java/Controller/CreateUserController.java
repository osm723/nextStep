package Controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpResponse;

public class CreateUserController implements Controller {

    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        User user = new User(
                request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("email")
        );
        log.info("===============" + user.getUserId());
        log.info("===============" + user.getPassword());
        DataBase.addUser(user);
        response.sendRedirect("/index.html");
    }
}

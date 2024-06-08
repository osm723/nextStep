package Controller;

import Controller.Controller;

import java.util.HashMap;
import java.util.Map;

public class RequestMapping {
    private static final Map<String, Controller> controllers = new HashMap<String, Controller>();

    static {
        controllers.put("/user/create", new CreateUserController());
        controllers.put("/user/login", new LoginController());
        controllers.put("/user/list", new ListUserController());
        controllers.put("/hello", new HelloController());
    }

    public static Controller getController(String requestUrl) {
        return controllers.get(requestUrl);
    }
}

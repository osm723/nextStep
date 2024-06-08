package Controller;

import db.DataBase;
import model.User;
import util.HttpRequest;
import util.HttpResponse;

import java.util.Collection;

public class HelloController implements Controller {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("hello");

        response.forwardBody(sb.toString());
    }
}

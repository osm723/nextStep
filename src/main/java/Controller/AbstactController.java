package Controller;

import model.HttpMethod;
import util.HttpRequest;
import util.HttpResponse;

public abstract class AbstactController implements Controller {

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        HttpMethod method = request.getMethod();

        if(method.isPost()) {
            doPost(request, response);
        } else {
            doGet(request, response);
        }
    }

    private void doGet(HttpRequest request, HttpResponse response) {
    }

    private void doPost(HttpRequest request, HttpResponse response) {
    }
}

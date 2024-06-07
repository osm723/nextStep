
import org.junit.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HttpRequestTest {

    private String testDir = "./src/test/resources/";


    @Test
    public void request_GET() throws Exception {
        String testFileName = "Http_GET.txt";
        String targetUrl = "http://localhost:8080";
        InputStream in = new FileInputStream(new File(testDir + testFileName));

        URL url = new URL(targetUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        //HttpRequest httpRequest = HttpRequest.newBuilder().build();

        System.out.println("connection = " + connection.getHeaderFields());

        assertThat("GET", is(connection.getRequestMethod().toString()));
        //assertThat("/user/create", is(connection.getURL().toString()));
        //assertThat("keep-alive", is(connection.getHeaderField("Connection")));
        //assertThat("oh", is(connection.getRequestProperty("userId")));
    }
}

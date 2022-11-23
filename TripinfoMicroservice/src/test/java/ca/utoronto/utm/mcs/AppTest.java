package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.json.JSONObject;

/**
 * Please write your tests in this class. 
 */
 
public class AppTest {

    /*private static final String API_URL = "http://localhost:8080";

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static int passenger1;
    public static int passenger2;

    private MongoDao dao;*/

    @Test
    public void exampleTest() {
        assertTrue(true);
    }

    @Test
    public void tripRequestPass(){

    }
}

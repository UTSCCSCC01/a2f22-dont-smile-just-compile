package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

/**
 * Please write your tests in this class. 
 */
 
public class AppTest {

    private static final String API_URL = "http://localhost:8004";

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws InterruptedException, IOException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    public void userLoginPass() throws JSONException, IOException, InterruptedException {
        String email = UUID.randomUUID().toString();
        JSONObject reqBody = new JSONObject()
                .put("name", "name")
                .put("password", "password")
                .put("email", email);
        HttpResponse<String> confirmRes = sendRequest("/user/register", "POST", reqBody.toString());
        assertEquals(200, confirmRes.statusCode());
        reqBody = new JSONObject()
                .put("password", "password")
                .put("email", email);
        confirmRes = sendRequest("/user/login", "POST", reqBody.toString());
        assertEquals(200, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("OK", response.get("status"));
    }

    @Test
    public void userLoginFail() throws IOException, InterruptedException, JSONException {
        String email = UUID.randomUUID().toString();
        JSONObject reqBody = new JSONObject()
                .put("email", email);
        HttpResponse<String> confirmRes = sendRequest("/user/login", "POST", reqBody.toString());
        assertEquals(400, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("BAD REQUEST", response.get("status"));
        reqBody = new JSONObject()
                .put("password", "password")
                .put("email", email);
        confirmRes = sendRequest("/user/login", "POST", reqBody.toString());
        assertEquals(404, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("NOT FOUND", response.get("status"));
        reqBody = new JSONObject()
                .put("name", "name")
                .put("password", "password")
                .put("email", email);
        confirmRes = sendRequest("/user/register", "POST", reqBody.toString());
        assertEquals(200, confirmRes.statusCode());
        reqBody = new JSONObject()
                .put("password", "wrong password")
                .put("email", email);
        confirmRes = sendRequest("/user/login", "POST", reqBody.toString());
        assertEquals(401, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("UNAUTHORIZED", response.get("status"));
    }

    @Test
    public void userRegisterPass() throws JSONException, IOException, InterruptedException {
        String email = UUID.randomUUID().toString();
        JSONObject reqBody = new JSONObject()
                .put("name", "name")
                .put("password", "password")
                .put("email", email);
        HttpResponse<String> confirmRes = sendRequest("/user/register", "POST", reqBody.toString());
        assertEquals(200, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("OK", response.get("status"));
    }

    @Test
    public void userRegisterFail() throws JSONException, IOException, InterruptedException {
        String email = UUID.randomUUID().toString();
        JSONObject reqBody = new JSONObject()
                .put("password", "password")
                .put("email", email);
        HttpResponse<String> confirmRes = sendRequest("/user/register", "POST", reqBody.toString());
        assertEquals(400, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("BAD REQUEST", response.get("status"));
        reqBody = new JSONObject()
                .put("name", "cooler name")
                .put("password", "cooler password")
                .put("email", email);
        sendRequest("/user/register", "POST", reqBody.toString());
        confirmRes = sendRequest("/user/register", "POST", reqBody.toString());
        assertEquals(409, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("CONFLICT", response.get("status"));
    }
}

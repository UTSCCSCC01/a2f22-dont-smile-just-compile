package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public abstract class Endpoint implements HttpHandler {

    public MongoDao dao;
    public HashMap<Integer, String> errorMap;

    private static final String apiGatewayPath = "http://apigateway:8000";

    public Endpoint() {
        this.dao = new MongoDao();
        errorMap = new HashMap<>();
        errorMap.put(200, "OK");
        errorMap.put(400, "BAD REQUEST");
        errorMap.put(401, "UNAUTHORIZED");
        errorMap.put(404, "NOT FOUND");
        errorMap.put(405, "METHOD NOT ALLOWED");
        errorMap.put(409, "CONFLICT");
        errorMap.put(500, "INTERNAL SERVER ERROR");
    }

    public void handle(HttpExchange r) {
        r.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // For CORS
        try {
            switch (r.getRequestMethod()) {
            case "OPTIONS":
                this.handleCors(r);
                break;
            case "GET":
                this.handleGet(r);
                break;
            case "PATCH":
                this.handlePatch(r);
                break;
            case "POST":
                this.handlePost(r);
                break;
            case "PUT":
                this.handlePut(r);
                break;
            case "DELETE":
                this.handleDelete(r);
                break;
            default:
                this.sendStatus(r, 405);
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeOutputStream(HttpExchange r, String response) throws IOException {
        OutputStream os = r.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public void sendResponse(HttpExchange r, JSONObject obj, int statusCode) throws JSONException, IOException {
        obj.put("status", errorMap.get(statusCode));
        String response = obj.toString();
        r.sendResponseHeaders(statusCode, response.length());
        this.writeOutputStream(r, response);
    }

    public void sendStatus(HttpExchange r, int statusCode) throws JSONException, IOException {
        JSONObject res = new JSONObject();
        res.put("status", errorMap.get(statusCode));
        String response = res.toString();
        r.sendResponseHeaders(statusCode, response.length());
        this.writeOutputStream(r, response);
    }

    public boolean validateFields(JSONObject JSONRequest, String[] fields, Class<?>[] fieldClasses) {
        for (int i = 0; i < fields.length; i++) {
            try {
                if (!JSONRequest.has(fields[i]) || !JSONRequest.get(fields[i]).getClass().equals(fieldClasses[i])) {
                    if (!(fieldClasses[i].equals(Double.class) && JSONRequest.get(fields[i]).getClass().equals(Integer.class))){
                        System.out.println(fields[i] + "failed validation, its class is " +  JSONRequest.get(fields[i]).getClass());
                        return false;
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public void handleCors(HttpExchange r) throws IOException {
        r.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        r.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        r.sendResponseHeaders(204, -1);
        return;
    }

    public void handleGet(HttpExchange r) throws IOException, JSONException {};

    public void handlePatch(HttpExchange r) throws IOException, JSONException {};

    public void handlePost(HttpExchange r) throws IOException, JSONException {};

    public void handlePut(HttpExchange r) throws IOException, JSONException {};

    public void handleDelete(HttpExchange r) throws IOException, JSONException {};

    public HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiGatewayPath + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
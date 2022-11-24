package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.UUID;
import org.json.JSONObject;

/**
 * Please write your tests in this class. 
 */
 
public class AppTest {

    private static final String API_URL = "http://localhost:8004";

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> sendRequestAsync(String endpoint, String method, String reqBody) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public String setup() throws JSONException, IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("driver", "1");
        requestBody.put("passenger", "2");
        requestBody.put("startTime", 100);
        HttpResponse<String> response = sendRequest("/trip/confirm", "POST", requestBody.toString());
        JSONObject responseObject = new JSONObject(response.body());

        String tripId = responseObject.getString("data");
        return tripId;
    }

    public JSONArray setupTripsForPassenger(String passenger) throws JSONException, IOException, InterruptedException {
        ArrayList<JSONObject> trips = new ArrayList<>();
        JSONObject requestBody = new JSONObject();
        JSONObject listItem = new JSONObject();

        requestBody.put("driver", "1");
        listItem.put("driver", "1");
        requestBody.put("passenger", passenger);
        requestBody.put("startTime", 100);
        listItem.put("startTime", 100);
        HttpResponse<String> res = sendRequest("/trip/confirm", "POST", requestBody.toString());
        listItem.put("_id", new JSONObject(res.body()).get("data"));

        trips.add(listItem);

        listItem = new JSONObject();
        requestBody = new JSONObject();
        listItem.put("driver", "2");
        requestBody.put("driver", "2");
        requestBody.put("passenger", passenger);
        requestBody.put("startTime", 200);
        listItem.put("startTime", 200);

        res = sendRequest("/trip/confirm", "POST", requestBody.toString());
        listItem.put("_id", new JSONObject(res.body()).get("data"));

        trips.add(listItem);

        return new JSONArray(trips);
    }

    public JSONArray setupTripsForDriver(String driver) throws JSONException, IOException, InterruptedException {
        ArrayList<JSONObject> trips = new ArrayList<>();
        JSONObject requestBody = new JSONObject();
        JSONObject listItem = new JSONObject();

        requestBody.put("driver", driver);
        listItem.put("passenger", "1");
        requestBody.put("passenger", "1");
        requestBody.put("startTime", 100);
        listItem.put("startTime", 100);
        HttpResponse<String> res = sendRequest("/trip/confirm", "POST", requestBody.toString());
        listItem.put("_id", new JSONObject(res.body()).get("data"));

        trips.add(listItem);

        listItem = new JSONObject();
        requestBody = new JSONObject();
        requestBody.put("driver", driver);
        listItem.put("passenger", "2");
        requestBody.put("passenger", "2");
        requestBody.put("startTime", 200);
        listItem.put("startTime", 200);

        res = sendRequest("/trip/confirm", "POST", requestBody.toString());
        listItem.put("_id", new JSONObject(res.body()).get("data"));

        trips.add(listItem);

        return new JSONArray(trips);
    }

    @Test
    public void exampleTest() {
        assertTrue(true);
    }

    @Test
    public void tripRequestPass(){
        JSONObject body = new JSONObject();

    }

    @Test
    public void tripRequestFail(){

    }

    @Test
    public void tripConfirmPass() throws JSONException, IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("driver", "1");
        requestBody.put("passenger", "2");
        requestBody.put("startTime", 2313);
        HttpResponse<String> response = sendRequest("/trip/confirm", "POST", requestBody.toString());
        assertEquals( 200, response.statusCode());
    }

    @Test
    public void tripConfirmFail() throws JSONException, IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("driver", "1");
        requestBody.put("passenger", "2");
        HttpResponse<String> response = sendRequest("/trip/confirm", "POST", requestBody.toString());
        assertEquals( 400, response.statusCode());
    }

    @Test
    public void patchTripPass() throws JSONException, IOException, InterruptedException {
        String tripId = setup();
        JSONObject requestBody = new JSONObject();
        requestBody.put("driver", "121");
        requestBody.put("distance", 234);
        HttpResponse<String> response = sendRequest("/trip/" + tripId, "PATCH", requestBody.toString());
        assertEquals( 200, response.statusCode());
    }

    @Test
    public void patchTripFail() throws JSONException, IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("driver", "121");
        requestBody.put("distance", 234);
        HttpResponse<String> response = sendRequest("/trip/ihopethisdriveridisnottaken", "PATCH", requestBody.toString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void tripsForPassengerPass() throws JSONException, IOException, InterruptedException {
        String passengerId = String.valueOf(Math.random());
        // TODO: does it matter if the uid for a trip endpoint isn't number? - Christine
        JSONArray trips = setupTripsForPassenger(passengerId);

        HttpResponse<String> response = sendRequest("/trip/passenger/" + passengerId, "GET", "");
        JSONObject responseBody = new JSONObject(response.body());

        assertEquals( trips, responseBody.getJSONObject("data").getJSONArray("trips"));
        System.out.println(trips);
        System.out.println(responseBody);
        assertEquals(200, response.statusCode());
    }

    @Test
    public void tripsForPassengerFail() throws JSONException, IOException, InterruptedException {
        String passengerId = Math.random() + "IHOPETHISPASSENGERDOESNTEXIST";
        // TODO: does it matter if the uid for a trip endpoint isn't number? - Christine


        HttpResponse<String> response = sendRequest("/trip/passenger/" + passengerId, "GET", "");
        assertEquals( 404, response.statusCode());
    }

    @Test
    public void tripsForDriverPass() throws JSONException, IOException, InterruptedException {
        String driverId = String.valueOf(Math.random());
        // TODO: does it matter if the uid for a trip endpoint isn't number? - Christine
        JSONArray trips = setupTripsForDriver(driverId);

        HttpResponse<String> response = sendRequest("/trip/driver/" + driverId, "GET", "");
        JSONObject responseBody = new JSONObject(response.body());

        assertEquals( trips, responseBody.getJSONObject("data").getJSONArray("trips"));
        System.out.println(trips);
        System.out.println(responseBody);
        assertEquals( 200, response.statusCode());
    }

    @Test
    public void tripsForDriverFail() throws JSONException, IOException, InterruptedException {
        String driverId = Math.random() + "IHOPETHISDRIVERDOESNTEXIST";
        // TODO: does it matter if the uid for a trip endpoint isn't number? - Christine


        HttpResponse<String> response = sendRequest("/trip/driver/" + driverId, "GET", "");
        assertEquals( 404, response.statusCode());
    }

    @Test
    public void driverTimePass(){

    }

    @Test
    public void driverTimeFail(){

    }
}

package ca.utoronto.utm.mcs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    public void createDriverAndPassenger() throws JSONException, IOException, InterruptedException {
        /**
         *                 String name = requestBody.getString("name");
         *                 String email = requestBody.getString("email");
         *                 String password = requestBody.getString("password");
         */
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "gdfg");
        requestBody.put("email", (int)(Math.random() * 10000) + "sdfsdf@gmail.com");
        requestBody.put("password", "password");
        sendRequest("/user/register", "POST", requestBody.toString());
        requestBody = new JSONObject();
        requestBody.put("name", "gdfg");
        requestBody.put("email", (int)(Math.random() * 10000) + "sdfafddsdfasfsasdf@gmail.com");
        requestBody.put("password", "password");
        sendRequest("/user/register", "POST", requestBody.toString());
        requestBody = new JSONObject();
        requestBody.put("name", "gdfg");
        requestBody.put("email", (int)(Math.random() * 10000) + "sdfafddsdfasfsasdf@gmail.com");
        requestBody.put("password", "password");
        sendRequest("/user/register", "POST", requestBody.toString());
        requestBody = new JSONObject();
        // make sure there exists a driver
        requestBody.put("isDriver", true);
        sendRequest("/user/1", "PATCH", requestBody.toString());
        sendRequest("/user/2", "PATCH", requestBody.toString());

    }

    public String setup() throws JSONException, IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("driver", "1");
        requestBody.put("passenger", "2");
        requestBody.put("startTime", 100);
        HttpResponse<String> response = sendRequest("/trip/confirm", "POST", requestBody.toString());
        JSONObject responseObject = new JSONObject(response.body());
        System.out.println(responseObject);
        String tripId = responseObject.getJSONObject("data").getString("_id");
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
        listItem.put("_id", new JSONObject(res.body()).getJSONObject("data").getString("_id"));

        trips.add(listItem);

        listItem = new JSONObject();
        requestBody = new JSONObject();
        listItem.put("driver", "2");
        requestBody.put("driver", "2");
        requestBody.put("passenger", passenger);
        requestBody.put("startTime", 200);
        listItem.put("startTime", 200);

        res = sendRequest("/trip/confirm", "POST", requestBody.toString());
        listItem.put("_id", new JSONObject(res.body()).getJSONObject("data").getString("_id"));

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
        listItem.put("_id", new JSONObject(res.body()).getJSONObject("data").getString("_id"));

        trips.add(listItem);

        listItem = new JSONObject();
        requestBody = new JSONObject();
        requestBody.put("driver", driver);
        listItem.put("passenger", "1");
        requestBody.put("passenger", "1");
        requestBody.put("startTime", 200);
        listItem.put("startTime", 200);

        res = sendRequest("/trip/confirm", "POST", requestBody.toString());
        listItem.put("_id", new JSONObject(res.body()).getJSONObject("data").getString("_id"));

        trips.add(listItem);

        return new JSONArray(trips);
    }

    @Test
    public void tripRequestPass() throws JSONException, IOException, InterruptedException {
        // This test requires getNearbyDriverPass() from the location app tests
        String driverUid = "1";
        String passengerUid = "2";
        JSONObject reqBody = new JSONObject()
                .put("uid", passengerUid)
                .put("radius", 1);
        HttpResponse<String> confirmRes = sendRequest("/trip/request/", "POST", reqBody.toString());
        assertEquals(200, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("OK", response.get("status"));
        JSONArray data = response.getJSONArray("data");
        assertEquals(1, data.length());
        assertEquals(driverUid, data.getString(0));
    }

    @Test
    public void tripRequestFail() throws JSONException, IOException, InterruptedException {
        String passengerUid = String.valueOf(((int)(Math.random() * 100000000)));
        JSONObject reqBody = new JSONObject()
                .put("uid", passengerUid)
                .put("radius", 30);
        HttpResponse<String> confirmRes = sendRequest("/trip/request/", "POST", reqBody.toString());
        assertEquals(404, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("NOT FOUND", response.get("status"));
        reqBody = new JSONObject()
                .put("id", passengerUid)
                .put("radius", 30);
        confirmRes = sendRequest("/trip/request/", "POST", reqBody.toString());
        assertEquals(400, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("BAD REQUEST", response.get("status"));
        reqBody = new JSONObject()
                .put("uid", passengerUid)
                .put("radius", -20);
        confirmRes = sendRequest("/trip/request/", "POST", reqBody.toString());
        assertEquals(400, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("BAD REQUEST", response.get("status"));

    }

    @Test
    public void tripConfirmPass() throws JSONException, IOException, InterruptedException {
        createDriverAndPassenger();
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
        /* {
            "distance": 23.4,
            "endTime": 34,
            "timeElapsed": "sdf",
            "discount": 34.4,
            "totalCost": 32432.0,
            "driverPayout": 324.0
        }*/
        requestBody.put("distance", 234.4);
        requestBody.put("endTime", 234);
        requestBody.put("timeElapsed", "4:453");
        requestBody.put("discount", 234);
        requestBody.put("totalCost", 234);
        requestBody.put("driverPayout", 234.5);

        HttpResponse<String> response = sendRequest("/trip/" + tripId, "PATCH", requestBody.toString());
        assertEquals( 200, response.statusCode());
    }

    @Test
    public void patchTripFail() throws JSONException, IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("distance", 234.4);
        requestBody.put("endTime", "time is a string");
        requestBody.put("timeElapsed", "4:453");
        requestBody.put("discount", 234);
        requestBody.put("totalCost", 234);
        requestBody.put("driverPayout", 234.5);
        HttpResponse<String> response = sendRequest("/trip/ihopethisdriveridisnottaken", "PATCH", requestBody.toString());
        assertEquals(400, response.statusCode());
    }

    @Test
    public void tripsForPassengerPass() throws JSONException, IOException, InterruptedException {
        createDriverAndPassenger();

        String passengerId = "3";
        // TODO: does it matter if the uid for a trip endpoint isn't number? - Christine
        JSONArray trips = setupTripsForPassenger(passengerId);

        HttpResponse<String> response = sendRequest("/trip/passenger/" + passengerId, "GET", "");
        JSONObject responseBody = new JSONObject(response.body());

        assertEquals( trips.toString(), responseBody.getJSONObject("data").getJSONArray("trips").toString());
        System.out.println(trips);
        System.out.println(responseBody);
        assertEquals(200, response.statusCode());
    }

    @Test
    public void tripsForPassengerFail() throws JSONException, IOException, InterruptedException {
        String passengerId = (int)(Math.floor(Math.random() * 10000000)) + "";
        // TODO: does it matter if the uid for a trip endpoint isn't number? - Christine


        HttpResponse<String> response = sendRequest("/trip/passenger/" + passengerId, "GET", "");
        assertEquals( 404, response.statusCode());
    }

    @Test
    public void tripsForDriverPass() throws JSONException, IOException, InterruptedException {
        createDriverAndPassenger();

        String driverId = "2";
        // TODO: does it matter if the uid for a trip endpoint isn't number? - Christine
        JSONArray trips = setupTripsForDriver(driverId);

        HttpResponse<String> response = sendRequest("/trip/driver/" + driverId, "GET", "");
        JSONObject responseBody = new JSONObject(response.body());

        //
        // compareJson(trips.getJSONObject(0), responseBody.get("data").getJSONArray("trips").getJSONObject(0));
        System.out.println(responseBody);
        assertEquals(trips.toString(),responseBody.getJSONObject("data").getJSONArray("trips").toString());
        System.out.println(trips);
        System.out.println(responseBody);
        assertEquals( 200, response.statusCode());
    }

    @Test
    public void tripsForDriverFail() throws JSONException, IOException, InterruptedException {
        String driverId = (int)(Math.floor(Math.random() * 10000000)) + "";
        // TODO: does it matter if the uid for a trip endpoint isn't number? - Christine


        HttpResponse<String> response = sendRequest("/trip/driver/" + driverId, "GET", "");
        assertEquals( 404, response.statusCode());
    }

    @Test
    public void driverTimePass() throws JSONException, IOException, InterruptedException {
        // This test requires getNavigationPass() from the location app tests
        String driverUid = "5";
        String passengerUid = "6";
        JSONObject requestBody = new JSONObject()
                .put("driver", driverUid)
                .put("passenger", passengerUid)
                .put("startTime", 0);
        HttpResponse<String> tripResponse = sendRequest("/trip/confirm", "POST", requestBody.toString());
        assertEquals( 200, tripResponse.statusCode());
        JSONObject trip = new JSONObject(tripResponse.body()).getJSONObject("data");
        HttpResponse<String> confirmRes = sendRequest("/trip/driverTime/" + trip.getString("_id"), "GET", "");
        assertEquals(200, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("OK", response.get("status"));
        JSONObject data = response.getJSONObject("data");
        assertEquals(6, data.getInt("arrival_time"));
    }

    @Test
    public void driverTimeFail() throws IOException, InterruptedException, JSONException {
        String tripId = UUID.randomUUID().toString();
        HttpResponse<String> confirmRes = sendRequest("/trip/driverTime/" + tripId, "GET", "");
        assertEquals(404, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("NOT FOUND", response.get("status"));
    }
}

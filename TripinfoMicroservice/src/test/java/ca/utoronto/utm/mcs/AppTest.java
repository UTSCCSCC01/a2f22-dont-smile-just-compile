package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mongodb.util.JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;
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
        listItem.put("passenger", "2");
        requestBody.put("passenger", "2");
        requestBody.put("startTime", 200);
        listItem.put("startTime", 200);

        res = sendRequest("/trip/confirm", "POST", requestBody.toString());
        listItem.put("_id", new JSONObject(res.body()).getJSONObject("data").getString("_id"));

        trips.add(listItem);

        return new JSONArray(trips);
    }

    public void addUser(JSONObject reqBody) throws IOException, InterruptedException, JSONException {
        HttpResponse<String> confirmRes = sendRequest("/location/user", "PUT", reqBody.toString());
        assertEquals(200, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("OK", response.get("status"));
    }

    public void updateUserLocation(String uid, JSONObject reqBody) throws IOException, InterruptedException, JSONException {
        HttpResponse<String> confirmRes = sendRequest("/location/" + uid, "PATCH", reqBody.toString());
        assertEquals(200, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("OK", response.get("status"));
    }

    public void compareJson(JSONObject expected, JSONObject actual) throws JSONException {
        Iterator<?> iterator = expected.keys();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            assertTrue(actual.has(key.toString()));
            assertEquals(expected.get(key.toString()), actual.get(key.toString()));
        }
    }

    public void compareJson(JSONArray expected, JSONArray actual) throws JSONException {
        assertEquals(expected.length(), actual.length());
        ArrayList<String> expectedValues = new ArrayList<>();
        for (int i = 0; i < expected.length() ; i++) {
            expectedValues.add(expected.get(i).toString());
        }
        for (int i = 0; i < actual.length(); i++) {
            assertTrue(expectedValues.contains(actual.get(i).toString()));
        }
    }

    public String setupTripRequest(String driverUid, String passengerUid) throws JSONException, IOException, InterruptedException {
        JSONObject driver = new JSONObject()
                .put("uid", driverUid)
                .put("is_driver", true);
        JSONObject passenger = new JSONObject()
                .put("uid", passengerUid)
                .put("is_driver", false);
        addUser(driver);
        addUser(passenger);
        double locX = 0.5;
        double locY = 0.5;
        JSONObject loc1 = new JSONObject()
                .put("longitude", locX)
                .put("latitude", locY)
                .put("street", "cool street");
        JSONObject loc2 = new JSONObject()
                .put("longitude", locX + 1)
                .put("latitude", locY + 20)
                .put("street", "not so cool street");
        updateUserLocation(driverUid, loc1);
        updateUserLocation(passengerUid, loc2);

        JSONObject reqBody = new JSONObject()
                .put("uid", passengerUid)
                .put("radius", 30);
        return reqBody.toString();
    }

    public void addRoad(JSONObject reqBody) throws IOException, InterruptedException, JSONException {
        HttpResponse<String> confirmRes = sendRequest("/location/road", "PUT", reqBody.toString());
        assertEquals(200, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("OK", response.get("status"));
    }

    public void hasRoute(JSONObject reqBody) throws IOException, InterruptedException, JSONException {
        HttpResponse<String> confirmRes = sendRequest("/location/hasRoute", "POST", reqBody.toString());
        assertEquals(200, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("OK", response.get("status"));
    }

    public void setupDrivetime(String driverUid, String passengerUid) throws JSONException, IOException, InterruptedException {
        JSONObject driver = new JSONObject()
                .put("uid", driverUid)
                .put("is_driver", true);
        JSONObject passenger = new JSONObject()
                .put("uid", passengerUid)
                .put("is_driver", false);
        addUser(driver);
        addUser(passenger);
        JSONObject road1 = new JSONObject()
                .put("roadName", "road1")
                .put("hasTraffic", true);
        JSONObject road2 = new JSONObject()
                .put("roadName", "road2")
                .put("hasTraffic", true);
        JSONObject road3 = new JSONObject()
                .put("roadName", "road3")
                .put("hasTraffic", false);
        JSONObject road4 = new JSONObject()
                .put("roadName", "road4")
                .put("hasTraffic", true);
        JSONObject toad = new JSONObject()
                .put("roadName", "toad")
                .put("hasTraffic", false);
        addRoad(road1);
        addRoad(road2);
        addRoad(road3);
        addRoad(road4);
        addRoad(toad);
        JSONObject route1 = new JSONObject()
                .put("roadName1", "road1")
                .put("roadName2", "road2")
                .put("hasTraffic", true)
                .put("time", 1);
        JSONObject route2 = new JSONObject()
                .put("roadName1", "road2")
                .put("roadName2", "road3")
                .put("hasTraffic", false)
                .put("time", 2);
        JSONObject route3 = new JSONObject()
                .put("roadName1", "road3")
                .put("roadName2", "road4")
                .put("hasTraffic", false)
                .put("time", 3);
        JSONObject route4 = new JSONObject()
                .put("roadName1", "road1")
                .put("roadName2", "toad")
                .put("hasTraffic", true)
                .put("time", 4);
        JSONObject route5 = new JSONObject()
                .put("roadName1", "toad")
                .put("roadName2", "road4")
                .put("hasTraffic", false)
                .put("time", 5);
        hasRoute(route1);
        hasRoute(route2);
        hasRoute(route3);
        hasRoute(route4);
        hasRoute(route5);
        JSONObject loc1 = new JSONObject()
                .put("longitude",0.5)
                .put("latitude", 0.5)
                .put("street", "road1");
        JSONObject loc2 = new JSONObject()
                .put("longitude", 1.5)
                .put("latitude", 0.5)
                .put("street", "road4");
        updateUserLocation(driverUid, loc1);
        updateUserLocation(passengerUid, loc2);
    }

    @Test
    public void tripRequestPass() throws JSONException, IOException, InterruptedException {
        String uid1 = UUID.randomUUID().toString();
        String uid2 = UUID.randomUUID().toString();
        String reqBody = setupTripRequest(uid1, uid2);
        HttpResponse<String> confirmRes = sendRequest("/trip/request/", "POST", reqBody);
        assertEquals(200, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("OK", response.get("status"));
        JSONArray data = response.getJSONArray("data");
        assertEquals(1, data.length());
        assertEquals(uid1, data.getString(0));
    }

    @Test
    public void tripRequestFail() throws JSONException, IOException, InterruptedException {
        String passengerUid = UUID.randomUUID().toString();
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
        JSONObject passenger = new JSONObject()
                .put("uid", passengerUid)
                .put("is_driver", false);
        addUser(passenger);
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
        requestBody.put("discount", 234.2);
        requestBody.put("totalCost", 234.4);
        requestBody.put("driverPayout", 234.5);

        HttpResponse<String> response = sendRequest("/trip/" + tripId, "PATCH", requestBody.toString());
        assertEquals( 200, response.statusCode());
    }

    @Test
    public void patchTripFail() throws JSONException, IOException, InterruptedException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("driver", "121");
        requestBody.put("distance", 234);
        HttpResponse<String> response = sendRequest("/trip/ihopethisdriveridisnottaken", "PATCH", requestBody.toString());
        assertEquals(400, response.statusCode());
    }

    @Test
    public void tripsForPassengerPass() throws JSONException, IOException, InterruptedException {
        String passengerId = String.valueOf((int)(Math.floor(Math.random() * 10000000)));
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
        String driverId = String.valueOf((int)(Math.floor(Math.random() * 10000000)));
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
        String driverUid = UUID.randomUUID().toString();
        String passengerUid = UUID.randomUUID().toString();
        setupDrivetime(driverUid, passengerUid);
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

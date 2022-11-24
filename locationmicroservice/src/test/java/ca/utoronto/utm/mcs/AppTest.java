package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * Please write your tests in this class. 
 */
 
public class AppTest {

    private static final String API_URL = "http://localhost:8000";

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws InterruptedException, IOException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public void addUser(JSONObject reqBody) throws IOException, InterruptedException, JSONException {
        HttpResponse<String> confirmRes = sendRequest("/location/user", "PUT", reqBody.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("OK", response.get("status"));
    }

    public void updateUserLocation(String uid, JSONObject reqBody) throws IOException, InterruptedException, JSONException {
        HttpResponse<String> confirmRes = sendRequest("/location/" + uid, "PATCH", reqBody.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("OK", response.get("status"));
    }

    public void addRoad(JSONObject reqBody) throws IOException, InterruptedException, JSONException {
        HttpResponse<String> confirmRes = sendRequest("/location/road", "PUT", reqBody.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("OK", response.get("status"));
    }

    public void hasRoute(JSONObject reqBody) throws IOException, InterruptedException, JSONException {
        HttpResponse<String> confirmRes = sendRequest("/location/hasRoute", "POST", reqBody.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("OK", response.get("status"));
    }

    public void compareJson(JSONObject expected, JSONObject actual) throws JSONException {
        for (Iterator it = expected.keys(); it.hasNext(); ) {
            Object key = it.next();
            assertTrue(actual.has(key.toString()));
            assertEquals(expected.get(key.toString()), actual.get(key.toString()));
        }
    }

    @Test
    public void getNearbyDriverPass() throws JSONException, IOException, InterruptedException {
        String uid1 = UUID.randomUUID().toString();
        String uid2 = UUID.randomUUID().toString();
        JSONObject user1 = new JSONObject()
                .put("uid", uid1)
                .put("is_driver", true);
        JSONObject user2 = new JSONObject()
                .put("uid", uid2)
                .put("is_driver", true);
        addUser(user1);
        addUser(user2);
        JSONObject loc1 = new JSONObject()
                .put("longitude", 21.5)
                .put("latitude", 20.5)
                .put("street", "cool street");
        JSONObject loc2 = new JSONObject()
                .put("longitude", 20.5)
                .put("latitude", 20.5)
                .put("street", "cool street");
        updateUserLocation(uid1, loc1);
        updateUserLocation(uid2, loc2);
        HttpResponse<String> confirmRes = sendRequest("/location/nearbyDriver/" + uid1 + "?radius=1", "GET", "");
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        JSONObject nearbyDrivers = (JSONObject) response.get("data");
        assertEquals("OK", response.get("status"));
        assertEquals(1, nearbyDrivers.length());
        assertTrue(nearbyDrivers.has(uid2));
        compareJson(loc2, nearbyDrivers.getJSONObject(uid2));
        confirmRes = sendRequest("/location/nearbyDriver/" + uid2 + "?radius=1", "GET", "");
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        nearbyDrivers = (JSONObject) response.get("data");
        assertEquals("OK", response.get("status"));
        assertEquals(1, nearbyDrivers.length());
        assertTrue(nearbyDrivers.has(uid1));
        compareJson(loc1, nearbyDrivers.getJSONObject(uid1));
    }

    @Test
    public void getNearbyDriverFail() throws JSONException, IOException, InterruptedException {
        String uid1 = UUID.randomUUID().toString();
        String uid2 = UUID.randomUUID().toString();
        HttpResponse<String> confirmRes = sendRequest("/location/nearbyDriver/" + uid1 + "?radius=1", "GET", "");
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("NOT FOUND", response.get("status"));
        JSONObject user1 = new JSONObject()
                .put("uid", uid1)
                .put("is_driver", true);
        JSONObject user2 = new JSONObject()
                .put("uid", uid2)
                .put("is_driver", true);
        addUser(user1);
        confirmRes = sendRequest("/location/nearbyDriver/" + uid1 + "?radius=1", "GET", "");
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("NOT FOUND", response.get("status"));
        addUser(user2);
        JSONObject loc1 = new JSONObject()
                .put("longitude", 1.5)
                .put("latitude", 0.5)
                .put("street", "cool street");
        JSONObject loc2 = new JSONObject()
                .put("longitude", 0.5)
                .put("latitude", 0.5)
                .put("street", "cool street");
        updateUserLocation(uid1, loc1);
        updateUserLocation(uid2, loc2);
        confirmRes = sendRequest("/location/nearbyDriver/" + uid1 + "?radius=0", "GET", "");
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("NOT FOUND", response.get("status"));
        confirmRes = sendRequest("/location/nearbyDriver/" + uid1 + "?radius=a", "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("BAD REQUEST", response.get("status"));
        confirmRes = sendRequest("/location/nearbyDriver/?radius=0", "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("BAD REQUEST", response.get("status"));
    }

    @Test
    public void getNavigationPass() throws JSONException, IOException, InterruptedException {
        String driverUid = UUID.randomUUID().toString();
        String passengerUid = UUID.randomUUID().toString();
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
                .put("longitude", 1.5)
                .put("latitude", 0.5)
                .put("street", "road1");
        JSONObject loc2 = new JSONObject()
                .put("longitude", 0.5)
                .put("latitude", 0.5)
                .put("street", "road4");
        updateUserLocation(driverUid, loc1);
        updateUserLocation(passengerUid, loc2);
        HttpResponse<String> confirmRes = sendRequest("/location/navigation/" + driverUid + "?passengerUid=" + passengerUid, "GET", "");
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("OK", response.get("status"));
        JSONObject data = (JSONObject) response.get("data");;
        assertEquals(2, data.length());
        assertEquals(6, data.getInt("total_time"));
        JSONObject[] expected = {
                new JSONObject()
                        .put("street", "road1")
                        .put("time", 0)
                        .put("is_traffic", true),
                new JSONObject()
                        .put("street", "road2")
                        .put("time", 1)
                        .put("is_traffic", true),
                new JSONObject()
                        .put("street", "road3")
                        .put("time", 2)
                        .put("is_traffic", false),
                new JSONObject()
                        .put("street", "road4")
                        .put("time", 3)
                        .put("is_traffic", true)
        };
        JSONArray route = data.getJSONArray("route");
        for (int i = 0; i < route.length(); i++) {
            compareJson(expected[i], route.getJSONObject(i));
        }
    }

    @Test
    public void getNavigationFail() throws JSONException, IOException, InterruptedException {
        String driverUid = UUID.randomUUID().toString();
        String passengerUid = UUID.randomUUID().toString();
        JSONObject driver = new JSONObject()
                .put("uid", driverUid)
                .put("is_driver", true);
        JSONObject passenger = new JSONObject()
                .put("uid", passengerUid)
                .put("is_driver", false);
        HttpResponse<String> confirmRes = sendRequest("/location/navigation/" + driverUid + "?passengerUid=" + passengerUid, "GET", "");
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
        JSONObject response = new JSONObject(confirmRes.body());
        assertEquals("NOT FOUND", response.get("status"));
        addUser(driver);
        addUser(passenger);
        JSONObject road5 = new JSONObject()
                .put("roadName", "road5")
                .put("hasTraffic", true);
        JSONObject road6 = new JSONObject()
                .put("roadName", "road6")
                .put("hasTraffic", true);
        confirmRes = sendRequest("/location/navigation/" + driverUid + "?passengerUid=" + passengerUid, "GET", "");
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("NOT FOUND", response.get("status"));
        addRoad(road5);
        addRoad(road6);
        JSONObject route1 = new JSONObject()
                .put("roadName1", "road5")
                .put("roadName2", "road6")
                .put("hasTraffic", true)
                .put("time", 6);
        confirmRes = sendRequest("/location/navigation/" + driverUid + "?passengerUid=" + passengerUid, "GET", "");
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("NOT FOUND", response.get("status"));
        hasRoute(route1);
        JSONObject loc1 = new JSONObject()
                .put("longitude", 1.5)
                .put("latitude", 0.5)
                .put("street", "road5");
        JSONObject loc2 = new JSONObject()
                .put("longitude", 0.5)
                .put("latitude", 0.5)
                .put("street", "road6");
        confirmRes = sendRequest("/location/navigation/" + driverUid + "?passengerUid=" + passengerUid, "GET", "");
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("NOT FOUND", response.get("status"));
        updateUserLocation(driverUid, loc1);
        updateUserLocation(passengerUid, loc2);
        confirmRes = sendRequest("/location/navigation/" + passengerUid + "?passengerUid=" + driverUid, "GET", "");
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("NOT FOUND", response.get("status"));
        confirmRes = sendRequest("/location/navigation/" + driverUid + "?uid=" + passengerUid, "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("BAD REQUEST", response.get("status"));
        confirmRes = sendRequest("/location/navigation/" + driverUid + "?passengerUid=", "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("BAD REQUEST", response.get("status"));
        confirmRes = sendRequest("/location/navigation/?passengerUid=" + passengerUid, "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
        response = new JSONObject(confirmRes.body());
        assertEquals("BAD REQUEST", response.get("status"));
    }
}

package ca.utoronto.utm.mcs;

import com.mongodb.client.FindIterable;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Iterator;

import org.json.JSONObject;

public class Passenger extends Endpoint {

    /**
     * GET /trip/passenger/:uid
     * @param uid
     * @return 200, 400, 404
     * Get all trips the passenger with the given uid has.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // TODO
        System.out.println("GOT request for passenger");
        JSONObject response = new JSONObject();
        int status;
        String passenger = r.getRequestURI().toString().substring("/trip/passenger/".length());
        System.out.println(passenger);
        if (passenger.length() > 0){
            boolean goAhead;
            try {
                HttpResponse<String> res = sendRequest("/user/" + passenger, "GET", "");
                if (res.statusCode() != 200){
                    goAhead = false;
                } else {
                    goAhead = true;
                }
                JSONArray trips = new JSONArray();

                JSONObject tripInfo;

                for (Document trip : this.dao.getTripsByFilter("passenger", passenger)) {
                    tripInfo = new JSONObject();
                    String[] fieldsStr = new String[]{"_id", "driver", "timeElapsed"};
                    String[] fieldsDoub = new String[]{ "distance", "driverPayout", "startTime", "endTime"};
                    for (String key: fieldsStr){
                        tripInfo.put(key, trip.get(key));
                    }
                    for (String key: fieldsDoub){
                        if (trip.get(key) != null) {
                            tripInfo.put(key, Double.parseDouble(trip.get(key).toString()));
                        }
                    }
                    trips.put(tripInfo);
                }
                if (trips.length() == 0 && !goAhead){
                    status = 404;
                } else {
                    JSONObject data = new JSONObject();
                    data.put("trips", trips);
                    System.out.println(data);
                    response.put("data", data);
                    status = 200;
                }

            } catch (Exception e){
                e.printStackTrace();
                status = 500;
            }
        } else {
            status = 400;
        }
        System.out.println("SENDING RESPONSE PASSENGER");
        sendResponse(r, response, status);
    }
}

package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.http.HttpResponse;

public class Passenger extends Endpoint {

    /**
     * GET /trip/passenger/:uid
     * @param uid
     * @return 200, 400, 404
     * Get all trips the passenger with the given uid has.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        try {
            JSONObject response = new JSONObject();
            int status;
            String passenger = r.getRequestURI().toString().substring("/trip/passenger/".length());
            if (passenger.length() > 0){
                try {
                    HttpResponse<String> res = sendRequest("/user/" + passenger, "GET", "");
                    boolean goAhead = res.statusCode() == 200;
                    JSONArray trips = new JSONArray();

                    JSONObject tripInfo;

                    for (Document trip : this.dao.getTripsByFilter("passenger", passenger)) {
                        tripInfo = new JSONObject();
                        String[] fieldsStr = new String[]{"_id", "driver", "timeElapsed"};
                        String[] fieldsDoub = new String[]{"distance", "totalCost", "discount", "startTime", "endTime"};
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
            sendResponse(r, response, status);
        } catch (Exception e){
            sendStatus(r, 500);
        }
    }
}

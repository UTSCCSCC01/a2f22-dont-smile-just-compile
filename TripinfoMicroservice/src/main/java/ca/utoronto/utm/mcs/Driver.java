package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import java.io.IOException;
import java.net.http.HttpResponse;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

public class Driver extends Endpoint {

    /**
     * GET /trip/driver/:uid
     * @param uid
     * @return 200, 400, 404
     * Get all trips driver with the given uid has.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        try {
            int status;
            JSONObject response = new JSONObject();
            String driver = r.getRequestURI().toString().substring("/trip/driver/".length());
            if (driver.length() > 0){
                try {
                    HttpResponse<String> res = sendRequest("/user/" + driver, "GET", "");
                    boolean goAhead = res.statusCode() == 200;

                    JSONArray trips = new JSONArray();
                    JSONObject tripInfo;
                    for (Document trip : this.dao.getTripsByFilter("driver", driver)) {
                        tripInfo = new JSONObject();
                        String[] fieldsStr = new String[]{"_id", "passenger", "timeElapsed"};
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

package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import java.io.IOException;

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
        // TODO
        JSONObject response = new JSONObject();
        int status;
        String driver = r.getRequestURI().toString().substring("/trip/driver/".length());
        System.out.println(driver);
        if (driver.length() > 0){
            JSONArray trips = new JSONArray();
            // TODO: 404 status code - Christine
            JSONObject tripInfo;
            for (Document trip : this.dao.getTripsByFilter("driver", driver)) {
                tripInfo = new JSONObject();
                String[] fields = new String[]{"_id", "distance", "totalCost", "discount", "startTime", "endTime", "timeElapsed", "passenger"};
                for (String key: fields){
                    tripInfo.put(key, trip.get(key));
                }
                trips.put(tripInfo);

            }
            JSONObject data = new JSONObject();
            data.put("trips", trips);
            response.put("data", data);
            status = 200;

        } else {
            status = 400;
        }
        sendResponse(r, response, status);
    }
}

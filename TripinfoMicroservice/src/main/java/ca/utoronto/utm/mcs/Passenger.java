package ca.utoronto.utm.mcs;

import com.mongodb.client.FindIterable;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
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
    public void handleGet(HttpExchange r) throws IOException,JSONException{
        // TODO
        System.out.println("GOT request for passenger");
        JSONObject response = new JSONObject();
        int status;
        String passenger = r.getRequestURI().toString().substring("/trip/passenger/".length());
        System.out.println(passenger);
        if (passenger.length() > 0){
            JSONArray trips = new JSONArray();

            JSONObject tripInfo;

            for (Document trip : this.dao.getTripsByFilter("passenger", passenger)) {
                tripInfo = new JSONObject();
                String[] fieldsStr = new String[]{"_id", "passenger"};
                String[] fieldsDoub = new String[]{ "distance", "driverPayout", "startTime", "endTime", "timeElapsed"};
                for (String key: fieldsStr){
                    tripInfo.put(key, trip.get(key));
                }
                for (String key: fieldsDoub){
                    tripInfo.put(key, trip.get(key));
                }
                trips.put(tripInfo);
            }
            JSONObject data = new JSONObject();
            data.put("trips", trips);
            System.out.println(data);
            response.put("data", data);
            status = 200;

        } else {
            status = 400;
        }
        System.out.println("SENDING RESPONSE PASSENGER");
        sendResponse(r, response, status);
    }
}

package ca.utoronto.utm.mcs;

/** 
 * Everything you need in order to send and recieve httprequests to 
 * other microservices is given here. Do not use anything else to send 
 * and/or recieve http requests from other microservices. Any other 
 * imports are fine.
 */
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import javax.print.Doc;
import java.io.IOException;

public class Drivetime extends Endpoint {

    /**
     * GET /trip/driverTime/:_id
     * @param _id
     * @return 200, 400, 404, 500
     * Get time taken to get from driver to passenger on the trip with
     * the given _id. Time should be obtained from navigation endpoint
     * in location microservice.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // TODO
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[3].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        try {
            System.out.println(r.getRequestURI());
            String tripId = params[3];
            Document tripInfo;
            try {
                tripInfo = this.dao.getTripByFilter("_id", new ObjectId(tripId));
            } catch(IllegalArgumentException e) {
                e.printStackTrace();
                this.sendStatus(r, 404);
                return;
            }

            if (tripInfo != null) {
                String driverUid = tripInfo.getString("driver");
                String passengerUid = tripInfo.getString("passenger");

                String endpoint = String.format("/location/navigation/%s?passengerUid=%s", driverUid, passengerUid);
                HttpResponse<String> res = sendRequest(endpoint, "GET", "");
                JSONObject resBody = new JSONObject(res.body());

                if (res.statusCode() == 200) {
                    JSONObject response = new JSONObject();
                    JSONObject arrivalTime = new JSONObject();

                    arrivalTime.put("arrival_time", resBody.getJSONObject("data").getInt("total_time"));

                    response.put("data", arrivalTime);
                    sendResponse(r, response, 200);
                } else {
                    sendStatus(r, res.statusCode());
                }
            } else {
                System.out.println("bad id " + tripId);
                sendStatus(r, 400);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}

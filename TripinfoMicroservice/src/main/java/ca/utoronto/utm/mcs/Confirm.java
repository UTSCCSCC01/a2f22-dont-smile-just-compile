package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import java.io.IOException;

import org.json.JSONObject;


public class Confirm extends Endpoint {

    /**
     * POST /trip/confirm
     * @body driver, passenger, startTime
     * @return 200, 400
     * Adds trip info into the database after trip has been requested.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // TODO
        int status;
        try {
            JSONObject requestBody = new JSONObject(Utils.convert(r.getRequestBody()));
            JSONObject response = new JSONObject();
            if (validateFields(requestBody, new String[]{"driver", "passenger", "startTime"},
                    new Class[]{String.class, String.class, Integer.class})){
                String driverUid = requestBody.getString("driver");
                String passengerUid = requestBody.getString("passenger");
                String startTime = requestBody.getString("startTime");
                String newId;
                if ((newId = this.dao.postTrip(driverUid, passengerUid, startTime)) != null){
                    status = 200;
                    System.out.println(newId);
                    JSONObject data = new JSONObject();
                    data.put("_id", newId );
                    response.put("data", data);
                } else {
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

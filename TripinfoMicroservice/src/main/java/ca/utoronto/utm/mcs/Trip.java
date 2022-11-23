package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.bson.types.ObjectId;
import org.json.JSONException;
import java.io.IOException;

import org.json.JSONObject;

public class Trip extends Endpoint {

    /**
     * PATCH /trip/:_id
     * @param _id
     * @body distance, endTime, timeElapsed, totalCost
     * @return 200, 400, 404
     * Adds extra information to the trip with the given id when the 
     * trip is done. 
     */

    @Override
    public void handlePatch(HttpExchange r) throws IOException, JSONException {
        // TODO
        System.out.println("Handling patch");
        JSONObject requestBody = new JSONObject(Utils.convert(r.getRequestBody()));
        JSONObject response = new JSONObject();
        int status;

        /** test data
         * {
         *     "distance": 1002,
         *     "endTime": 200000000,
         *     "timeElapsed": 303,
         *     "discount": 5.3,
         *     "totalCost": 345.23,
         *     "driverPayout": 0.0
         * }
         */
        String tripId = r.getRequestURI().toString().substring("/trip/".length());
        if (this.dao.getTripByFilter("_id", new ObjectId(tripId)) != null){
            String[] bodyParams = new String[]{"distance", "endTime", "timeElapsed", "discount",
                    "totalCost", "driverPayout"};
            if (validateFields(requestBody, bodyParams, new Class[]{Double.class, Integer.class, Integer.class,
                    Double.class, Double.class, Double.class})){
                try {
                    Double distance = requestBody.getDouble("distance");
                    Integer endTime = requestBody.getInt("endTime");
                    Integer timeElapsed = requestBody.getInt("timeElapsed");
                    Double discount = requestBody.getDouble("discount");
                    Double totalCost = requestBody.getDouble("totalCost");
                    Double driverPayout = requestBody.getDouble("driverPayout");

                    this.dao.patchTrip(tripId, distance, endTime, timeElapsed, discount, totalCost, driverPayout);

                    status = 200;
                } catch (Exception e){
                    e.printStackTrace();
                    status = 500;
                }

            } else {
                status = 400;
            }
        } else {
            status = 404;
        }
        sendResponse(r, response, status);
    }
}

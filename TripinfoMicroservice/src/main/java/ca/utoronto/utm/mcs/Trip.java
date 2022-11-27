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
        try {
            JSONObject requestBody = new JSONObject(Utils.convert(r.getRequestBody()));
            JSONObject response = new JSONObject();
            int status;

            String tripId = r.getRequestURI().toString().substring("/trip/".length());
            ObjectId tripObjectId;
            try {
                tripObjectId = new ObjectId(tripId);
            } catch (IllegalArgumentException e){
                sendStatus(r, 400);
                return;
            }
            if (this.dao.getTripByFilter("_id", tripObjectId) != null){
                String[] bodyParams = new String[]{"distance", "endTime", "timeElapsed", "discount",
                        "totalCost", "driverPayout"};
                if (validateFields(requestBody, bodyParams, new Class[]{Integer.class, Integer.class, String.class,
                        Integer.class, Integer.class, Integer.class}) ||
                        validateFields(requestBody, bodyParams, new Class[]{Double.class, Integer.class, String.class,
                                Double.class, Double.class, Double.class})
                ){
                    try {
                        double distance = requestBody.getDouble("distance");
                        int endTime = requestBody.getInt("endTime");
                        String timeElapsed = requestBody.getString("timeElapsed");
                        double discount = requestBody.getDouble("discount");
                        double totalCost = requestBody.getDouble("totalCost");
                        double driverPayout = requestBody.getDouble("driverPayout");

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
        } catch (Exception e){
            sendStatus(r, 500);
        }
    }
}

package ca.utoronto.utm.mcs;

/** 
 * Everything you need in order to send and recieve httprequests to 
 * other microservices is given here. Do not use anything else to send 
 * and/or recieve http requests from other microservices. Any other 
 * imports are fine.
 */

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Request extends Endpoint {

    /**
     * POST /trip/request
     * @body uid, radius
     * @return 200, 400, 404, 500
     * Returns a list of drivers within the specified radius 
     * using location microservice. List should be obtained
     * from navigation endpoint in location microservice
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException,JSONException{
        try {
            JSONObject requestBody = new JSONObject(Utils.convert(r.getRequestBody()));
            JSONObject response = new JSONObject();

            if (this.validateFields(requestBody, new String[]{"uid", "radius"}, new Class[]{String.class, Integer.class})){
                String uid = requestBody.getString("uid");
                Integer radius = requestBody.getInt("radius");
                String endpoint = String.format("/location/nearbyDriver/%s?radius=%d", uid, radius);
                try {
                    HttpResponse<String> res = sendRequest(endpoint, "GET", "");
                    JSONObject resBody = new JSONObject(res.body());
                    if (res.statusCode() == 200) {
                        JSONObject bodyData = resBody.getJSONObject("data");
                        List<String> drivers = new ArrayList<>();
                        Iterator<?> iterator = bodyData.keys();
                        while(iterator.hasNext()){
                            drivers.add(iterator.next().toString());
                        }
                        response.put("data", drivers);
                        sendResponse(r, response, 200);
                    } else {
                        sendStatus(r, res.statusCode());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    sendStatus(r, 500);
                }
            } else {
                sendStatus(r, 400);
            }
        } catch (Exception e){
            sendStatus(r, 500);
        }

    }
}

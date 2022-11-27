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
import org.json.JSONException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.json.JSONObject;
import com.mongodb.client.FindIterable;
import org.bson.Document;

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
        // TODO
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
                        List<String> driverUids = new ArrayList<String>();
                        Iterator<?> iterator = bodyData.keys();
                        while(iterator.hasNext()){
                            driverUids.add(iterator.next().toString());
                        }
                        response.put("data", driverUids);
                        sendResponse(r, response, 200);
                    } else {
                        sendStatus(r, res.statusCode()); // there would be where 404 is returned
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

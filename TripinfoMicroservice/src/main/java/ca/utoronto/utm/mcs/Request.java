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

        System.out.println("HANDLING POST");
        System.out.println(r.getRequestURI());


        JSONObject requestBody = new JSONObject(Utils.convert(r.getRequestBody()));
        JSONObject response = new JSONObject();

        int status;
        if (this.validateFields(requestBody, new String[]{"uid", "radius"}, new Class[]{String.class, Integer.class})){
            String uid = requestBody.getString("uid");
            Integer radius = requestBody.getInt("radius");
            status = 200;
            // TODO: Fix everything below - Christine
            String driverRequest = String.format("http://localhost:8000/location/nearbyDriver/%s?radius=%d", uid, radius);
            // GET /location/nearbyDriver.:uid?radius=
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(driverRequest)).build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(new Function<HttpResponse<String>, Object>() {
                        @Override
                        public Object apply(HttpResponse<String> stringHttpResponse) {
                            try {
                                System.out.println(stringHttpResponse.body());
                                JSONObject body = new JSONObject(stringHttpResponse.body());
                                JSONObject bodyData = body.getJSONObject("data");
                                List<String> driverUids = new ArrayList<String>();
                                Iterator<?> iterator = bodyData.keys();
                                while(iterator.hasNext()){
                                    driverUids.add(iterator.next().toString());
                                }
                                response.put("data", driverUids);
                                sendResponse(r, response, 200);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    });
        } else {
            status = 400;
            this.sendResponse(r, response, status);
        }

    }
}

package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

public class Nearby extends Endpoint {
    
    /**
     * GET /location/nearbyDriver/:uid?radius=:radius
     * @param uid, radius
     * @return 200, 400, 404, 500
     * Get drivers that are within a certain radius around a user.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[3].isEmpty() || !params[3].matches(".+\\?radius=\\d+")) {
            this.sendStatus(r, HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        String[] uidRadius = params[3].split("\\?radius=");
        if (uidRadius.length != 2) {
            this.sendStatus(r, HttpURLConnection.HTTP_BAD_REQUEST); //should "hi?radius=?radius=0" be accepted? -Catherine
            return;
        }

        try {
//            String[] uidRadius = params[3].split("\\?radius=", 2);
            Result validUid = this.dao.getUserByUid(uidRadius[0]);
            if (validUid.hasNext()) {
                Result result = this.dao.getNearbyDrivers(uidRadius[0], Integer.parseInt(uidRadius[1]));

                if (result.hasNext()) {
                    JSONObject res = new JSONObject();
                    JSONObject data = new JSONObject();
                    do {
                        Record user = result.next();
                        String uid = user.get("uid").asString();
                        Map<String, Object> loc = user.get("loc").asMap();
                        data.put(uid, loc);
                    } while (result.hasNext());

                    res.put("data", data);
                    this.sendResponse(r, res, HttpURLConnection.HTTP_OK);
                } else {
                    this.sendStatus(r, HttpURLConnection.HTTP_NOT_FOUND);
                }
            } else {
                this.sendStatus(r, HttpURLConnection.HTTP_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }
}

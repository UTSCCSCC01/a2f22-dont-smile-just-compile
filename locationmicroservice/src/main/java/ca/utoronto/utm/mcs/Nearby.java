package ca.utoronto.utm.mcs;

import java.io.IOException;
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
        try {
            String[] params = r.getRequestURI().toString().split("/");
            if (params.length != 4 || params[3].isEmpty() || !params[3].matches("\\d+\\?radius=\\d+")) {
                this.sendStatus(r, 400);
                return;
            }

            String[] uidRadius = params[3].split("\\?radius=");
            if (this.dao.getUserByUid(uidRadius[0]).hasNext()) {
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
                    this.sendResponse(r, res, 200);
                    return;
                }
            }
            this.sendStatus(r, 404);
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}

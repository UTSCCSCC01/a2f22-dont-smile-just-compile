package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

public class Navigation extends Endpoint {
    
    /**
     * GET /location/navigation/:driverUid?passengerUid=:passengerUid
     * @param driverUid, passengerUid
     * @return 200, 400, 404, 500
     * Get the shortest path from a driver to passenger weighted by the
     * travel_time attribute on the ROUTE_TO relationship.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[3].isEmpty() || !params[3].matches(".+\\?passengerUid=.+")) {
            this.sendStatus(r, HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        String[] uids = params[3].split("\\?passengerUid=");
        if (uids.length != 2) {
            this.sendStatus(r, HttpURLConnection.HTTP_BAD_REQUEST);
            return;
        }

        try {
            JSONObject res = new JSONObject();
            Result result = this.dao.getShortestRoute(uids[0], uids[1]);
            int status;

            if (result.hasNext()) {
                Record user = result.next();
                Map<String, Object> data = user.get("data").asMap();
                res.put("data", data);
                status = HttpURLConnection.HTTP_OK;
            } else {
                status = HttpURLConnection.HTTP_NOT_FOUND;
            }
            res.put("status", status);
            this.sendResponse(r, res, status);
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }
}

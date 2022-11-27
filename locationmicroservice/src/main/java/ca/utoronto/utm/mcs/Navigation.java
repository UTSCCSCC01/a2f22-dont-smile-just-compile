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

        try {
            String[] params = r.getRequestURI().toString().split("/");
            if (params.length != 4 || params[3].isEmpty() || !params[3].matches("\\d+\\?passengerUid=\\d+")) {
                this.sendStatus(r, 400);
                return;
            }
            String[] driverPassenger = params[3].split("\\?passengerUid=");
            Result driver = this.dao.getUserLocationByUid(driverPassenger[0]);
            Result passenger = this.dao.getUserLocationByUid(driverPassenger[1]);
            if (driver.hasNext() && passenger.hasNext()) {
                String driverStreet = driver.next().get("n.street").asString();
                String passengerStreet = passenger.next().get("n.street").asString();
                if (driverStreet == null || passengerStreet == null) {
                    this.sendStatus(r, 400);
                } else if (driverStreet.equals(passengerStreet)) {
                    System.out.println(driverStreet + " is the same as " + passengerStreet);
                    JSONObject res = new JSONObject();
                    JSONObject data = new JSONObject()
                            .put("total_time", 0)
                            .put("route", new JSONArray());
                    res.put("data", data);
                    this.sendResponse(r, res, 200);
                } else {
                    Result result = this.dao.getShortestRoute(driverPassenger[0], driverPassenger[1]);

                    if (result.hasNext()) {
                        JSONObject res = new JSONObject();
                        Record user = result.next();
                        Map<String, Object> data = user.get("data").asMap();
                        res.put("data", data);
                        this.sendResponse(r, res, 200);
                    } else {
                        this.sendStatus(r, 404);
                    }
                }
            } else {
                this.sendStatus(r, 404);
            }

        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}

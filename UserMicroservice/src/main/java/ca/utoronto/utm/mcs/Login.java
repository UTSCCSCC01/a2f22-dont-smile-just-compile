package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends Endpoint {

    /**
     * POST /user/login
     * @body email, password
     * @return 200, 400, 401, 404, 500
     * Login a user into the system if the given information matches the 
     * information of the user in the database.
     */
    
    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // TODO
        JSONObject requestBody = new JSONObject(Utils.convert(r.getRequestBody()));
        JSONObject response = new JSONObject();
        int status;
        if (validateFields(requestBody, new String[]{"email", "password"},
                new Class[]{String.class, String.class})){

            String email = requestBody.getString("email");
            String password = requestBody.getString("password");

            try {
                ResultSet result;
                // TODO: 401 status code??? - Christine
                if (!this.dao.matchUser(email, null,null,null,null).next()){
                    status = 404;
                } else if ((result = this.dao.loginUser(email, password)).next()){
                    status = 200;
                    JSONObject data = new JSONObject();
                    response.put("uid", String.valueOf(result.getInt("uid")));
                } else { // email exists, but password doesnt match
                    status = 401;
                }
            } catch (SQLException e) {
                status = 500;
                e.printStackTrace();
            }

        } else {
            status = 400;
        }
        sendResponse(r, response, status);
    }
}

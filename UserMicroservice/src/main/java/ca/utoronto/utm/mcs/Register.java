package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

public class Register extends Endpoint {

    /**
     * POST /user/register
     * @body name, email, password
     * @return 200, 400, 500
     * Register a user into the system using the given information.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // TODO
        JSONObject requestBody = new JSONObject(Utils.convert(r.getRequestBody()));
        int status;
        JSONObject response = new JSONObject();

        if (validateFields(requestBody, new String[]{"name", "email", "password"},
                new Class[]{String.class, String.class, String.class})){
            String name = requestBody.getString("name");
            String email = requestBody.getString("email");
            String password = requestBody.getString("password");

            try {
                Integer uid;
                if ((uid = this.dao.registerUser(name, email, password)) != null){
                    status = 200;
                    JSONObject data = new JSONObject();
                    response.put("uid", uid);
                } else { // if email already has an account
                    status = 409;
                }

            } catch (SQLException e) {
                e.printStackTrace();
                status = 500;
            }

        } else {
            status = 400;
        }
        sendResponse(r, response, status);
    }
}

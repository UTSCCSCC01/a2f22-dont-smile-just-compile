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
        if (validateFields(requestBody, new String[]{"name", "email", "password"},
                new Class[]{String.class, String.class, String.class})){
            String name = requestBody.getString("name");
            String email = requestBody.getString("email");
            String password = requestBody.getString("password");

            try {
                if (this.dao.registerUser(name, email, password)){
                    status = 200;
                } else {
                    status = 409;
                }

            } catch (SQLException e) {
                e.printStackTrace();
                status = 500;
            }

        } else {
            status = 400;
        }
        sendStatus(r, status);
    }
}

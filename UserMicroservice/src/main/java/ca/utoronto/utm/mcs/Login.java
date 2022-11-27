package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
        try {
            JSONObject requestBody = new JSONObject(Utils.convert(r.getRequestBody()));
            JSONObject response = new JSONObject();
            int status;
            if (validateFields(requestBody, new String[]{"email", "password"},
                    new Class[]{String.class, String.class})){

                String email = requestBody.getString("email");
                String password = requestBody.getString("password");

                if (!this.dao.matchUser(email, null,null,null,null).next()){
                    status = 404;
                } else if ((this.dao.loginUser(email, password)).next()){
                    status = 200;
                } else {
                    status = 401;
                }

            } else {
                status = 400;
            }
            sendResponse(r, response, status);
        } catch (Exception e){
            sendStatus(r, 500);
        }
    }
}

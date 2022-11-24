package ca.utoronto.utm.mcs;

import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** 
 * Everything you need in order to send and recieve httprequests to 
 * the microservices is given here. Do not use anything else to send 
 * and/or recieve http requests from other microservices. Any other 
 * imports are fine.
 */
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.OutputStream;    // Also given to you to send back your response

import java.util.function.Function;

public class RequestRouter implements HttpHandler {
	
    /**
     * You may add and/or initialize attributes here if you 
     * need.
     */
	public RequestRouter() {

	}

	public void routeToService(HttpExchange r, String url, String method, String requestBody, OutputStream responseBody){
		System.out.println(url);
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.method(method, HttpRequest.BodyPublishers.ofString(requestBody))
				.build();
		client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.thenApply(new Function<HttpResponse<String>, Object>() {
					@Override
					public Object apply(HttpResponse<String> stringHttpResponse) {
						String serviceResponse;
						try {
							serviceResponse = stringHttpResponse.body();
							System.out.println(serviceResponse);
							int statusCode = stringHttpResponse.statusCode();
							r.sendResponseHeaders(statusCode, serviceResponse.length());
							responseBody.write(serviceResponse.getBytes());
							responseBody.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						return null;
					}
				});
	}

	// TODO: IDK HOW TO FIND THESE FROM DOCKER OR WHATEVER - Christine
	private static String locationMicroservicePath = "http://locationmicroservice:8000";
	private static String userMicroservicePath = "http://usermicroservice:8000";
	private static String tripInfoMicroservicePath = "http://tripinfomicroservice:8000";

	@Override
	public void handle(HttpExchange r) throws IOException {
        // TODO
		System.out.println(r.getRequestURI());
		String path = r.getRequestURI().getPath();
		System.out.println(path);
		String method = r.getRequestMethod();
		r.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		try {
			String servicePath;
			String requestBody = Utils.convert(r.getRequestBody());
			String service = path.substring(0, path.substring(1).indexOf("/") + 1);
			System.out.println("service");
			switch(service){
				case "/trip":
					servicePath = tripInfoMicroservicePath + path;
					break;
				case "/user":
					servicePath = userMicroservicePath + path;
					break;
				case "/location":
					servicePath = locationMicroservicePath + path;
					break;
				default:
					r.sendResponseHeaders(404, "NOT FOUND".length());
					r.getResponseBody().write("NOT FOUND".getBytes());
					r.getResponseBody().close();
					return;
			}
			routeToService(r, servicePath, method, requestBody, r.getResponseBody());
		} catch (Exception e) {
			e.printStackTrace();
			r.sendResponseHeaders(500, "INTERNAL SERVER ERROR".length());
			r.getResponseBody().write("INTERNAL SERVER ERROR".getBytes());
			r.getResponseBody().close();

		}
	}
}

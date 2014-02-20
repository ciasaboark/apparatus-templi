package org.apparatus_templi;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class DriverHandler implements HttpHandler {
    
	public void handle(HttpExchange exchange) throws IOException {
        byte[] response = "<?xml version=\"1.0\"?>\n<resource id=\"1234\" name=\"test\" />\n".getBytes();
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 
            response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    };
}

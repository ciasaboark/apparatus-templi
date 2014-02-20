package org.apparatus_templi;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class DriverHandler implements HttpHandler {
    
	public void handle(HttpExchange exchange) throws IOException {
		byte[] response = getResponse();
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    };
    
    public byte[] getResponse() {
    	/*
    	 * LedFlash is just a place holder for now. It will need to be replaced by the driver name
    	 * from the GET Request
    	 */    	
    	String xml = Coordinator.requestWidgetXML("LedFlash");
    	byte[] response = ("<?xml version=\"1.0\"?>\n" + xml).getBytes();
    	return response;
    }
}

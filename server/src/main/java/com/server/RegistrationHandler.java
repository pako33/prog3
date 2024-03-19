package com.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationHandler implements HttpHandler { 

    private final UserAuthenticator userAuthenticator;

    public RegistrationHandler(UserAuthenticator userAuthenticator) { 
        this.userAuthenticator = userAuthenticator;
    } 

    @Override
    public void handle(HttpExchange exchange) throws IOException { 
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
    
            try {
                JSONObject jsonObj = new JSONObject(requestBody);
                String username = jsonObj.getString("username");
                String password = jsonObj.getString("password");
                String email = jsonObj.getString("email");
                if (userAuthenticator.addUser(username, password, email)) {
                    String response = "User registered successfully";
                    exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                    }
                } else {
                    String response = "User already exists";
                    exchange.sendResponseHeaders(403, response.getBytes(StandardCharsets.UTF_8).length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                    }
                }
            } catch (JSONException e) {
                String response = "Invalid JSON";
                exchange.sendResponseHeaders(400, response.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        } else {
            String response = "Not supported";
            exchange.sendResponseHeaders(400, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
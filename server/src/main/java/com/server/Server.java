package com.server;

import com.sun.net.httpserver.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

public class Server implements HttpHandler {

    private final List<UserMessage> messages = new ArrayList<>();

    private Server() {
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            try (InputStream inputStream = exchange.getRequestBody()) {
                String requestBody = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                                        .lines().collect(Collectors.joining("\n"));
                JSONObject jsonObj = new JSONObject(requestBody);
                String locationName = jsonObj.getString("locationName");
                String locationDescription = jsonObj.getString("locationDescription");
                String locationCity = jsonObj.getString("locationCity");

                synchronized (this) {
                    messages.add(new UserMessage(locationName, locationDescription, locationCity));
                }
                exchange.sendResponseHeaders(200, -1);
            } catch (Exception e) {
                exchange.sendResponseHeaders(400, -1);
            }
        } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            JSONArray jsonArray = new JSONArray();
            synchronized (this) {
                for (UserMessage message : messages) {
                    JSONObject messageObj = new JSONObject();
                    messageObj.put("locationName", message.getLocationName());
                    messageObj.put("locationDescription", message.getLocationDescription());
                    messageObj.put("locationCity", message.getLocationCity());
                    jsonArray.put(messageObj);
                }
            }
            
            String jsonResponse = jsonArray.toString();
            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(bytes);
            }
        } else {
            String response = "Not supported";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, bytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(bytes);
            }
        }
    }

    private static SSLContext myServerSSLContext() throws Exception {
        char[] passphrase = "Replace_This".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("keystore.jks"), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        return ssl;
    }

    public static void main(String[] args) throws Exception {
        HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
        SSLContext sslContext = myServerSSLContext();
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
                SSLContext c = getSSLContext();
                SSLParameters sslParameters = c.getDefaultSSLParameters();
                params.setSSLParameters(sslParameters);
            }
        });

        UserAuthenticator authenticator = new UserAuthenticator();
        
        HttpContext infoContext = server.createContext("/info", new Server());
        infoContext.setAuthenticator(authenticator);
        
        server.createContext("/registration", new RegistrationHandler(authenticator));
        
        server.setExecutor(null); 
        System.out.println("HTTPS server started on port 8001");
        server.start();
    }
}
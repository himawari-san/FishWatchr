package org.teachothers.fishwatchr;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


public class RemoteControlServer {
    private final HttpServer server;
    private final Callback callback;
    
    public RemoteControlServer(int port, Callback callback) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        server.createContext("/play", new CommandHandler());
        server.setExecutor(null);
        this.callback = callback;
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
    
    private class CommandHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            URI uri = exchange.getRequestURI();
            String query = uri.getQuery();
            Map<String, String> params = parseQuery(query);
            
            long time = Long.parseLong(params.getOrDefault("time", "-1"));
            String commenter = params.getOrDefault("commenter", "");
            callback.callback(time);
            
            String response = "Playing " + time + "s";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

        }
    }
    
    
    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=");
            if (parts.length == 2) {
                result.put(parts[0], parts[1]);
            }
        }
        return result;
    }
}

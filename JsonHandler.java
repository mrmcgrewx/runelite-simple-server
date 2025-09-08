package net.runelite.client.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class JsonHandler implements HttpHandler
{
    private final Supplier supplier;

    JsonHandler(Supplier supplier)
    {
        this.supplier = supplier;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            byte[] body = supplier.get().getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
        } catch (Exception e) {
            byte[] body = ("{\"error\":\"" + e.getMessage() + "\"}").getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(500, body.length);
            ex.getResponseBody().write(body);
        } finally {
            ex.close();
        }
    }
}



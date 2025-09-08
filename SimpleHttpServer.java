package net.runelite.client.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.name.Named;
import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Instant;

@Slf4j
@Singleton
public class SimpleHttpServer {

    private HttpServer http;
    @Inject
    private String host;
    private int port;
    @Getter
    private final String defaultJson = "{\"status\":\"initialized. Login to begin.\"}";
    private volatile String latestJson =
            defaultJson;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .disableHtmlEscaping()
            .create();

    @Inject
    public SimpleHttpServer(
            @Named("simpleHttp.host") @Nullable String host,
            @Named("simpleHttp.port") @Nullable Integer port
    ) {
        this.host = host != null ? host : "127.0.0.1";
        this.port = port != null ? port : 0;
    }

    public void startHttp()
    {
        try
        {
            InetSocketAddress addr = new InetSocketAddress(host, port);
            http = HttpServer.create(addr, 0);
            http.createContext("/health", new JsonHandler(() -> "{\"ok\":true}"));
            http.createContext("/targets", new JsonHandler(() -> latestJson));
            http.setExecutor(null); // default executor
            http.start();
            log.info("HTTP listening on {}:{}", host, port);
        }
        catch (IOException e)
        {
            log.warn("Failed to start HTTP server: {}", e.getMessage());
        }
    }

    public void stopHttp()
    {
        if (http != null)
        {
            http.stop(0);
            http = null;
        }
    }

    public void setLatestJson(Payload payload) {
        latestJson = gson.toJson(payload);
    }

    public void setDefaultJson() {
        latestJson = gson.toJson(defaultJson);
    }
}

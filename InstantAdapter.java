package net.runelite.client.server;

// Put this somewhere shared (e.g., JsonHandler, SimpleHttpServer, or a util class)
import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.Instant;

public final class InstantAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext ctx) {
        return (src == null) ? JsonNull.INSTANCE : new JsonPrimitive(src.toString()); // e.g. 2025-09-07T03:05:06.789Z
    }
    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
        if (json == null || json.isJsonNull()) return null;
        return Instant.parse(json.getAsString());
    }
}

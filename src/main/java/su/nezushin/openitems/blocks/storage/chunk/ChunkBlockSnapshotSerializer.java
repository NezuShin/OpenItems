package su.nezushin.openitems.blocks.storage.chunk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class ChunkBlockSnapshotSerializer {

    private static final Gson GSON = new GsonBuilder().create();

    public byte[] encode(ChunkBlockSnapshot snapshot) {
        return GSON.toJson(snapshot).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public ChunkBlockSnapshot decode(byte[] bytes) {
        String json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        return GSON.fromJson(json, ChunkBlockSnapshot.class);
    }
}

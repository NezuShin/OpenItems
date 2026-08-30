package su.nezushin.openitems.blocks.storage.pdc;

import org.bukkit.Chunk;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.storage.chunk.ChunkBlockStoreFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public final class ChunkBlockPdcReader {

    public ChunkBlockStoreFormat detect(Chunk chunk) {
        var pdc = chunk.getPersistentDataContainer();
        Integer version = pdc.get(OpenItems.CUSTOM_BLOCKS_VERSION_KEY, PersistentDataType.INTEGER);
        if (version != null && version == ChunkBlockPdcKeys.FORMAT_VERSION)
            return ChunkBlockStoreFormat.V2_BLOB;

        return ChunkBlockStoreFormat.EMPTY;
    }

    /** Gunzip-combined payload (JSON bytes before gzip). */
    public byte[] readRaw(Chunk chunk) {
        var pdc = chunk.getPersistentDataContainer();
        Integer partCount = pdc.get(OpenItems.CUSTOM_BLOCKS_BPARTS_KEY, PersistentDataType.INTEGER);
        int shards = partCount != null ? partCount : 1;

        ByteArrayOutputStream combined = new ByteArrayOutputStream();
        for (int i = 0; i < shards; i++) {
            byte[] part = pdc.get(ChunkBlockPdcKeys.shardKey(i), PersistentDataType.BYTE_ARRAY);
            if (part == null)
                throw new IllegalStateException("Missing chunk block shard b" + i);
            combined.writeBytes(part);
        }

        try {
            return gunzip(combined.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress chunk block data", e);
        }
    }

    static byte[] gunzip(byte[] data) throws IOException {
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data))) {
            return gzip.readAllBytes();
        }
    }
}

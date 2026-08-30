package su.nezushin.openitems.blocks.storage.pdc;

import org.bukkit.Chunk;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import su.nezushin.openitems.OpenItems;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public final class ChunkBlockPdcWriter {

    public static final int MAX_BLOB_PART_BYTES = 60_000;

    public void write(Chunk chunk, byte[] rawPayload) {
        try {
            byte[] gzipped = gzip(rawPayload);
            List<byte[]> shards = split(gzipped, MAX_BLOB_PART_BYTES);

            var pdc = chunk.getPersistentDataContainer();
            clearExistingShards(pdc);

            pdc.set(OpenItems.CUSTOM_BLOCKS_VERSION_KEY, PersistentDataType.INTEGER, ChunkBlockPdcKeys.FORMAT_VERSION);

            if (shards.size() > 1)
                pdc.set(OpenItems.CUSTOM_BLOCKS_BPARTS_KEY, PersistentDataType.INTEGER, shards.size());
            else
                pdc.remove(OpenItems.CUSTOM_BLOCKS_BPARTS_KEY);

            for (int i = 0; i < shards.size(); i++)
                pdc.set(ChunkBlockPdcKeys.shardKey(i), PersistentDataType.BYTE_ARRAY, shards.get(i));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write chunk block data", e);
        }
    }

    public void clear(Chunk chunk) {
        var pdc = chunk.getPersistentDataContainer();
        clearExistingShards(pdc);
        pdc.remove(OpenItems.CUSTOM_BLOCKS_VERSION_KEY);
        pdc.remove(OpenItems.CUSTOM_BLOCKS_BPARTS_KEY);
    }

    private void clearExistingShards(PersistentDataContainer pdc) {
        Integer existingParts = pdc.get(OpenItems.CUSTOM_BLOCKS_BPARTS_KEY, PersistentDataType.INTEGER);
        int maxShards = existingParts != null ? existingParts : 64;
        for (int i = 0; i < maxShards; i++) {
            var key = ChunkBlockPdcKeys.shardKey(i);
            if (pdc.has(key, PersistentDataType.BYTE_ARRAY))
                pdc.remove(key);
            else if (existingParts == null && i > 0)
                break;
        }
    }

    static byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(data);
        }
        return baos.toByteArray();
    }

    static List<byte[]> split(byte[] data, int maxPartBytes) {
        List<byte[]> parts = new ArrayList<>();
        for (int i = 0; i < data.length; i += maxPartBytes) {
            int len = Math.min(maxPartBytes, data.length - i);
            parts.add(java.util.Arrays.copyOfRange(data, i, i + len));
        }
        if (parts.isEmpty())
            parts.add(new byte[0]);
        return parts;
    }
}

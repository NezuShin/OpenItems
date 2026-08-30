package su.nezushin.openitems.blocks.storage.chunk;

import org.bukkit.Chunk;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;

import java.util.ArrayList;
import java.util.List;

public final class ChunkBlockSnapshotDecoder {

    public List<BlockLocationStore> decode(Chunk chunk, ChunkBlockSnapshot snapshot) {
        ItemPalette palette = snapshot.toPalette();
        List<BlockLocationStore> stores = new ArrayList<>();

        if (snapshot.blocks == null)
            return stores;

        for (ChunkBlockRecord record : snapshot.blocks) {
            var item = palette.resolve(record.paletteIndex);
            int[] pos = ChunkBlockPositionCodec.unpack(chunk, record.packedPosition);
            var store = new BlockLocationStore(pos[0], pos[1], pos[2], item);
            if (!store.load())
                continue;
            store.getExtras().putAll(BlockExtraCodec.decode(record.extrasYaml));
            stores.add(store);
        }

        return stores;
    }
}

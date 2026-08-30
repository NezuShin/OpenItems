package su.nezushin.openitems.blocks.storage.chunk;

import org.bukkit.Chunk;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ChunkBlockSnapshotEncoder {

    public ChunkBlockSnapshot encode(Chunk chunk, Collection<BlockLocationStore> stores) {
        ItemPalette palette = new ItemPalette();
        List<ChunkBlockRecord> records = new ArrayList<>();

        for (BlockLocationStore store : stores) {
            ItemStack item = store.getCurrentItem().clone();
            item.setAmount(1);
            String itemYaml = ItemStackYamlSerializer.serialize(item);
            int paletteIndex = palette.intern(itemYaml);
            int packed = ChunkBlockPositionCodec.pack(chunk, store.getX(), store.getY(), store.getZ());
            String extrasYaml = BlockExtraCodec.encode(store.getExtras());
            records.add(new ChunkBlockRecord(paletteIndex, packed, extrasYaml));
        }

        return new ChunkBlockSnapshot(2, palette, records);
    }
}

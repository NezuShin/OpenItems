package su.nezushin.openitems.blocks.storage.chunk;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

final class ChunkBlockSnapshot {

    @SerializedName("v")
    int version;

    @SerializedName("palette")
    List<String> palette = new ArrayList<>();

    @SerializedName("blocks")
    List<ChunkBlockRecord> blocks = new ArrayList<>();

    ChunkBlockSnapshot() {
    }

    ChunkBlockSnapshot(int version, ItemPalette palette, List<ChunkBlockRecord> blocks) {
        this.version = version;
        this.palette = palette.getSerializedItems();
        this.blocks = blocks;
    }

    static ChunkBlockSnapshot empty() {
        return new ChunkBlockSnapshot(2, new ItemPalette(), List.of());
    }

    boolean isEmpty() {
        return blocks == null || blocks.isEmpty();
    }

    ItemPalette toPalette() {
        ItemPalette itemPalette = new ItemPalette();
        itemPalette.setSerializedItems(palette);
        return itemPalette;
    }
}

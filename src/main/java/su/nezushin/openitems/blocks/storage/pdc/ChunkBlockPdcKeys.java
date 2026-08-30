package su.nezushin.openitems.blocks.storage.pdc;

import org.bukkit.NamespacedKey;
import su.nezushin.openitems.OpenItems;

public final class ChunkBlockPdcKeys {

    public static final int FORMAT_VERSION = 2;
    public static final String SHARD_PREFIX = "custom_blocks_b";

    private ChunkBlockPdcKeys() {
    }

    public static NamespacedKey shardKey(int index) {
        return new NamespacedKey(OpenItems.getInstance(), SHARD_PREFIX + index);
    }
}

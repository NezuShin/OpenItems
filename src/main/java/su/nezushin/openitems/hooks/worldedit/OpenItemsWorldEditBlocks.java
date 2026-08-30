package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.World;
import org.bukkit.block.Block;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;

/**
 * Shared helpers for attaching OpenItems payloads to WorldEdit blocks.
 */
final class OpenItemsWorldEditBlocks {

    private OpenItemsWorldEditBlocks() {
    }

    static BaseBlock enrichFullBlock(World world, BlockVector3 position, BaseBlock block) {
        Block bukkitBlock = world.getBlockAt(position.x(), position.y(), position.z());
        BlockLocationStore store = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(bukkitBlock);
        if (store == null)
            return block;
        return OpenItemsWorldEditTag.tag(block, store.getCurrentItem());
    }
}

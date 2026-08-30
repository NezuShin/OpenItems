package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.World;

import java.util.function.BooleanSupplier;

/**
 * WorldEdit {@link BukkitWorld} wrapper that attaches OpenItems item payloads during block reads.
 *
 * <p>{@link com.sk89q.worldedit.EditSession#getFullBlock} reads from the session world directly,
 * bypassing the extent stack. Wrapping the world fixes {@code //copy}, {@code //cut},
 * {@code //schem save}, and similar snapshot operations.
 */
final class OpenItemsWorldEditWorld extends BukkitWorld {

    private final BooleanSupplier active;

    OpenItemsWorldEditWorld(World world, BooleanSupplier active) {
        super(world);
        this.active = active;
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        BaseBlock block = super.getFullBlock(position);
        if (!active.getAsBoolean())
            return block;
        return OpenItemsWorldEditBlocks.enrichFullBlock(getWorld(), position, block);
    }
}

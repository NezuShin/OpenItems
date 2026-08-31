package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.World;

import java.util.function.BooleanSupplier;

/**
 * WorldEdit {@link BukkitWorld} wrapper that attaches OpenItems item payloads during block reads.
 *
 * <p>Used by extended vanilla WorldEdit support ({@code enable-extended-support}). FAWE uses
 * {@link OpenItemsWorldEditExtent} read enrichment instead.
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
        return OpenItemsWorldEditTag.enrichFullBlock(getWorld(), position, block);
    }
}

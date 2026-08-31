package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.CustomBlocks;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;

import java.util.function.BooleanSupplier;

/**
 * Intercepts WorldEdit block reads and writes to keep OpenItems metadata in sync.
 *
 * <p>Writes (//set, paste placement, overwrite cleanup) are always handled here when basic support
 * is enabled. Reads for copy/schematic capture use either:
 * <ul>
 *   <li>FAWE mode — {@link #getFullBlock} / {@link #getBlock} enrichment on this extent</li>
 *   <li>Extended vanilla WE — {@link OpenItemsWorldEditWorld} world wrapper</li>
 * </ul>
 *
 * <p>Model application must happen in {@link EditSession.Stage#BEFORE_CHANGE} after the host block
 * has actually been written.
 */
final class OpenItemsWorldEditExtent extends AbstractDelegateExtent {

    private final World world;
    private final BooleanSupplier active;
    private final EditSession.Stage stage;
    private final boolean enrichReads;

    OpenItemsWorldEditExtent(
            Extent extent,
            World world,
            BooleanSupplier active,
            EditSession.Stage stage,
            boolean enrichReads
    ) {
        super(extent);
        this.world = world;
        this.active = active;
        this.stage = stage;
        this.enrichReads = enrichReads;
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        BlockState block = super.getBlock(position);
        if (!enrichReads || !active.getAsBoolean())
            return block;
        return enrich(block.toBaseBlock(), position).toImmutableState();
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        return enrich(super.getFullBlock(position), position);
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block) throws WorldEditException {
        return setBlockInternal(position, block);
    }

    private BaseBlock enrich(BaseBlock block, BlockVector3 position) {
        if (!enrichReads || !active.getAsBoolean())
            return block;
        return OpenItemsWorldEditTag.enrichFullBlock(world, position, block);
    }

    private <B extends BlockStateHolder<B>> boolean setBlockInternal(BlockVector3 position, B block)
            throws WorldEditException {
        if (!active.getAsBoolean())
            return super.setBlock(position, block);

        Block bukkitBlock = world.getBlockAt(position.x(), position.y(), position.z());
        CustomBlocks blocks = OpenItems.getInstance().getBlocks();
        BlockLocationStore existing = blocks.getPlacedBlocks().get(bukkitBlock);

        if (existing != null && !OpenItemsWorldEditTag.representsSameBlock(block, existing))
            blocks.destroyBlock(bukkitBlock, false, false);

        boolean result = super.setBlock(position, block);

        if (stage != EditSession.Stage.BEFORE_CHANGE)
            return result;

        ItemStack item = OpenItemsWorldEditTag.extractItem(block);
        if (item == null)
            return result;

        BlockLocationStore current = blocks.getPlacedBlocks().get(bukkitBlock);
        if (current == null || !OpenItemsWorldEditTag.representsSameBlock(block, current))
            blocks.registerWorldEditBlockMetadata(bukkitBlock, item);
        blocks.applyWorldEditModel(bukkitBlock);

        return result;
    }
}

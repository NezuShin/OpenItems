package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * FAWE edit extent. Vanilla WorldEdit keeps using {@link OpenItemsWorldEditExtent}.
 *
 * <p>{@code //set} calls {@code setBlocks}, which FAWE forwards past {@code setBlock} overrides.
 * The {@code setBlocks} methods below are declared without {@code @Override} because upstream
 * WorldEdit 7.3 does not have them; on FAWE they replace {@code AbstractDelegateExtent}'s forward.
 * {@code setBlock(int, int, int, BlockStateHolder)} is the same kind of runtime override.
 *
 * <p>Block-state models are baked into the holder FAWE queues. Registry updates and display
 * entities run on the main thread one tick after {@code commitBefore()}, once the queue has been flushed.
 * A constant vanilla fill still uses FAWE's own {@code setBlocks}; custom blocks inside that
 * region are dropped after the flush.
 */
final class OpenItemsFaweExtent extends AbstractDelegateExtent {

    private final World world;
    private final BooleanSupplier active;
    private final EditSession.Stage stage;
    private final List<OpenItemsFawePlacement.Change> pending = new ArrayList<>();
    private final List<Region> vanillaFills = new ArrayList<>();
    private Method vanillaSetBlocks;
    private boolean vanillaSetBlocksResolved;

    OpenItemsFaweExtent(Extent extent, World world, BooleanSupplier active, EditSession.Stage stage) {
        super(extent);
        this.world = world;
        this.active = active;
        this.stage = stage;
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        BlockState block = super.getBlock(position);
        if (!active.getAsBoolean())
            return block;
        return OpenItemsWorldEditTag.enrichFullBlock(world, position, block.toBaseBlock()).toImmutableState();
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        BaseBlock block = super.getFullBlock(position);
        if (!active.getAsBoolean())
            return block;
        return OpenItemsWorldEditTag.enrichFullBlock(world, position, block);
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block) throws WorldEditException {
        return place(position, block);
    }

    public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
        return place(BlockVector3.at(x, y, z), block);
    }

    public <B extends BlockStateHolder<B>> int setBlocks(Region region, B block) throws MaxChangedBlocksException {
        return setBlocksHolder(region, block);
    }

    public int setBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
        if (pattern instanceof BlockPattern blockPattern)
            return setBlocks(region, blockPattern.getBlock());
        if (pattern instanceof BlockStateHolder<?> holder)
            return setBlocksHolder(region, holder);
        return placeEach(region, pattern::applyBlock);
    }

    public int setBlocks(Set<BlockVector3> positions, Pattern pattern) throws MaxChangedBlocksException {
        if (positions instanceof Region region)
            return setBlocks(region, pattern);
        return placeEach(positions, pattern::applyBlock);
    }

    /**
     * {@code commit()} is final. This runs before the delegate flushes, so the actual
     * registry and display work is postponed until the next tick.
     */
    @Override
    protected Operation commitBefore() {
        List<OpenItemsFawePlacement.Change> batch = pending.isEmpty() ? List.of() : List.copyOf(pending);
        List<Region> fills = vanillaFills.isEmpty() ? List.of() : List.copyOf(vanillaFills);
        pending.clear();
        vanillaFills.clear();
        if (batch.isEmpty() && fills.isEmpty())
            return null;
        return new FlushOperation(world, batch, fills);
    }

    private int setBlocksHolder(Region region, BlockStateHolder<?> block) throws MaxChangedBlocksException {
        if (active.getAsBoolean() && !isOpenItems(block))
            return forwardVanillaFill(region, block);
        return placeRegion(region, block);
    }

    private static boolean isOpenItems(BlockStateHolder<?> block) {
        return OpenItemsWorldEditTag.extractItem(block) != null;
    }

    /**
     * Hand a non-OpenItems fill to FAWE's region writer. {@code setBlocks} exists on FAWE's
     * {@link Extent} only, so the call is reflective and compiled against vanilla WorldEdit.
     */
    private int forwardVanillaFill(Region region, BlockStateHolder<?> block) throws MaxChangedBlocksException {
        Extent target = faweDelegate();

        Region snapshot = active.getAsBoolean() ? region.clone() : null;
        if (snapshot != null)
            vanillaFills.add(snapshot);

        Method method = vanillaSetBlocks(target);
        if (method == null) {
            if (snapshot != null)
                vanillaFills.remove(snapshot);
            return placeRegion(region, block);
        }

        try {
            Object result = method.invoke(target, region, block);
            return result instanceof Integer count ? count : 0;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MaxChangedBlocksException limit)
                throw limit;
            if (cause instanceof RuntimeException runtime)
                throw runtime;
            throw new RuntimeException(cause);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Extent faweDelegate() {
        Extent current = getExtent();
        while (current instanceof OpenItemsFaweExtent nested)
            current = nested.getExtent();
        return current;
    }

    private Method vanillaSetBlocks(Extent target) {
        if (!vanillaSetBlocksResolved) {
            vanillaSetBlocksResolved = true;
            try {
                vanillaSetBlocks = target.getClass().getMethod("setBlocks", Region.class, BlockStateHolder.class);
            } catch (NoSuchMethodException ignored) {
                vanillaSetBlocks = null;
            }
        }
        return vanillaSetBlocks;
    }

    private int placeRegion(Region region, BlockStateHolder<?> block) throws MaxChangedBlocksException {
        return placeEach(region, position -> block);
    }

    private int placeEach(Iterable<BlockVector3> positions, Function<BlockVector3, BlockStateHolder<?>> blockAt)
            throws MaxChangedBlocksException {
        int count = 0;
        for (BlockVector3 position : positions) {
            try {
                if (place(position, blockAt.apply(position)))
                    count++;
            } catch (MaxChangedBlocksException e) {
                throw e;
            } catch (WorldEditException e) {
                throw new RuntimeException(e);
            }
        }
        return count;
    }

    private boolean place(BlockVector3 position, BlockStateHolder<?> block) throws WorldEditException {
        if (!active.getAsBoolean())
            return delegate(position, block);

        ItemStack item = OpenItemsWorldEditTag.extractItem(block);
        OpenItemsFaweBlockEncoder.Encoded encoded = item == null
                ? null
                : OpenItemsFaweBlockEncoder.encode(block, item);
        BlockStateHolder<?> outgoing = encoded == null ? block : encoded.block();

        boolean result = delegate(position, outgoing);
        if (stage == EditSession.Stage.BEFORE_CHANGE) {
            if (item == null)
                pending.add(OpenItemsFawePlacement.Change.clear(position.x(), position.y(), position.z()));
            else
                pending.add(new OpenItemsFawePlacement.Change(
                        position.x(), position.y(), position.z(), item.clone(), encoded));
        }
        return result;
    }

    private boolean delegate(BlockVector3 position, BlockStateHolder<?> block) throws WorldEditException {
        if (block instanceof BaseBlock baseBlock)
            return super.setBlock(position, baseBlock);
        return super.setBlock(position, block.toBaseBlock());
    }

    private static final class FlushOperation implements Operation {

        private final World world;
        private final List<OpenItemsFawePlacement.Change> changes;
        private final List<Region> vanillaFills;
        private boolean finished;

        private FlushOperation(World world, List<OpenItemsFawePlacement.Change> changes, List<Region> vanillaFills) {
            this.world = world;
            this.changes = changes;
            this.vanillaFills = vanillaFills;
        }

        @Override
        public Operation resume(RunContext run) {
            if (!finished) {
                finished = true;
                Bukkit.getScheduler().runTaskLater(OpenItems.getInstance(), () -> {
                    OpenItemsFawePlacement.finish(world, changes);
                    OpenItemsFawePlacement.sweep(world, vanillaFills);
                }, 1L);
            }
            return null;
        }

        @Override
        public void cancel() {
            finished = true;
        }
    }
}

package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.CustomBlocks;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;
import su.nezushin.openitems.blocks.types.CustomArbitraryBlockModel;
import su.nezushin.openitems.blocks.types.CustomBlockModel;
import su.nezushin.openitems.blocks.types.CustomSlabBlockModel;
import su.nezushin.openitems.blocks.types.CustomStairsBlockModel;
import su.nezushin.openitems.utils.NBTUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Applies OpenItems registry updates and display entities on the main thread after FAWE flushes.
 */
final class OpenItemsFawePlacement {

    private static final int MAX_DISPLAY_ATTEMPTS = 40;

    private OpenItemsFawePlacement() {
    }

    record Change(int x, int y, int z, ItemStack item, OpenItemsFaweBlockEncoder.Encoded encoded) {
        static Change clear(int x, int y, int z) {
            return new Change(x, y, z, null, null);
        }

        String overrideId() {
            return encoded == null ? null : encoded.overrideId();
        }

        boolean spawnDisplay() {
            return encoded != null && encoded.spawnDisplay();
        }
    }

    static void finish(World world, List<Change> changes) {
        if (!OpenItems.getInstance().isEnabled())
            return;

        CustomBlocks blocks = OpenItems.getInstance().getBlocks();
        List<Change> displays = new ArrayList<>();

        for (Change change : changes) {
            Block block = world.getBlockAt(change.x(), change.y(), change.z());
            if (change.item() == null) {
                if (blocks.getPlacedBlocks().containsKey(block))
                    blocks.destroyBlock(block, false, false);
                continue;
            }

            String itemId = NBTUtil.getBlockId(change.item());
            if (itemId == null)
                continue;

            BlockLocationStore existing = blocks.getPlacedBlocks().get(block);
            boolean replaced = existing == null || !itemId.equals(existing.getId());
            if (replaced) {
                if (existing != null)
                    blocks.destroyBlock(block, false, false);
                existing = blocks.registerWorldEditBlockMetadata(block, change.item());
            }

            boolean overrideChanged = existing != null
                    && !Objects.equals(change.overrideId(), existing.getOverrideId());
            if (overrideChanged) {
                if (!replaced) {
                    CustomBlockModel previous = existing.getCurrentModel();
                    if (previous != null)
                        previous.remove(block);
                }
                existing.setOverrideId(change.overrideId());
                existing.applyData();
                blocks.saveChunk(block.getChunk());
            }

            if (change.spawnDisplay())
                displays.add(change);
            else
                blocks.applyWorldEditModel(block);
        }

        spawnDisplays(world, displays, 0);
    }

    /**
     * Drop OpenItems data for blocks overwritten by a vanilla FAWE fill.
     * The fast path does not visit each block, so this runs once after the queue flushes.
     */
    static void sweep(World world, List<Region> regions) {
        if (regions.isEmpty() || !OpenItems.getInstance().isEnabled())
            return;

        CustomBlocks blocks = OpenItems.getInstance().getBlocks();
        List<Block> remove = new ArrayList<>();
        for (Block block : blocks.getPlacedBlocks().keySet()) {
            if (!block.getWorld().getUID().equals(world.getUID()))
                continue;
            BlockVector3 position = BlockVector3.at(block.getX(), block.getY(), block.getZ());
            for (Region region : regions) {
                if (region.contains(position)) {
                    remove.add(block);
                    break;
                }
            }
        }

        for (Block block : remove)
            blocks.destroyBlock(block, false, false);
    }

    private static void spawnDisplays(World world, List<Change> displays, int attempt) {
        if (displays.isEmpty() || !OpenItems.getInstance().isEnabled())
            return;

        CustomBlocks blocks = OpenItems.getInstance().getBlocks();
        List<Change> retry = new ArrayList<>();

        for (Change change : displays) {
            Block block = world.getBlockAt(change.x(), change.y(), change.z());
            if (blocks.getPlacedBlocks().get(block) == null)
                continue;
            if (!hostReady(block, change.item())) {
                if (attempt < MAX_DISPLAY_ATTEMPTS)
                    retry.add(change);
                continue;
            }
            blocks.applyWorldEditModel(block);
        }

        if (!retry.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(
                    OpenItems.getInstance(),
                    () -> spawnDisplays(world, retry, attempt + 1),
                    1L);
        }
    }

    private static boolean hostReady(Block block, ItemStack item) {
        String id = NBTUtil.getBlockId(item);
        CustomBlockModel model = id == null
                ? null
                : OpenItems.getInstance().getModelRegistry().getBlockTypes().get(id);
        if (model instanceof CustomStairsBlockModel)
            return block.getBlockData() instanceof Stairs;
        if (model instanceof CustomSlabBlockModel)
            return block.getBlockData() instanceof Slab slab && slab.getType() != Slab.Type.DOUBLE;
        if (model instanceof CustomArbitraryBlockModel)
            return block.getType() == model.resolveHostMaterial(item);
        return true;
    }
}

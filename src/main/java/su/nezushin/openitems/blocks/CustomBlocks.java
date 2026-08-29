package su.nezushin.openitems.blocks;

import com.google.common.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;
import su.nezushin.openitems.blocks.types.CustomChorusModel;
import su.nezushin.openitems.blocks.types.CustomTripwireModel;
import su.nezushin.openitems.events.CustomBlockLoadEvent;
import su.nezushin.openitems.events.CustomBlockUnloadEvent;
import su.nezushin.openitems.gson.ConfigurationSerializableGsonAdapter;
import su.nezushin.openitems.utils.BlockEntityUtil;
import su.nezushin.openitems.utils.NBTUtil;
import su.nezushin.openitems.utils.OpenItemsConfig;

import java.util.*;

public class CustomBlocks {

    //All loaded blocks in server
    private Map<Block, BlockLocationStore> placedBlocks = new HashMap<>();

    // ItemDisplay entities for stairs/display blocks (ephemeral)
    private Map<Block, ItemDisplay> displayEntities = new HashMap<>();

    //Blocks need to be destroyed on next chunk load
    private Map<Block, DestroyOnLoadBlock> destroyOnLoad = new HashMap<>();

    private Map<Chunk, Integer> saveChunkDebounce = new HashMap<>();

    private BlockBreakSpeedModifiers blockBreakSpeedModifiers;


    private record DestroyOnLoadBlock(boolean dropItem, boolean setAir, Runnable callback) {

    }

    public CustomBlocks() {
        removeAllDisplayEntities();
        Bukkit.getPluginManager().registerEvents(new CustomBlocksListener(), OpenItems.getInstance());

        blockBreakSpeedModifiers = new BlockBreakSpeedModifiers();

        for (var world : Bukkit.getWorlds())
            for (var chunk : world.getLoadedChunks())
                loadChunk(chunk);
    }

    public Map<Block, BlockLocationStore> getPlacedBlocks() {
        return placedBlocks;
    }

    public Map<Block, ItemDisplay> getDisplayEntities() {
        return displayEntities;
    }


    public void saveChunk(Chunk chunk) {
        if (this.saveChunkDebounce.containsKey(chunk)) {
            Bukkit.getScheduler().cancelTask(this.saveChunkDebounce.get(chunk));
        }
        Runnable saveRunnable = () -> {
            this.saveChunkDebounce.remove(chunk);
            List<BlockLocationStore> list = new ArrayList<>(this.placedBlocks.entrySet()
                    .stream().filter(i -> i.getKey().getChunk().equals(chunk)).map(Map.Entry::getValue).toList());

            OpenItems.async(() -> {
                var str = ConfigurationSerializableGsonAdapter.createGson().toJson(list);
                OpenItems.sync(() -> {
                    chunk.getPersistentDataContainer().set(OpenItems.CUSTOM_BLOCKS_CHUNK_KEY, PersistentDataType.STRING, str);
                });
            });
        };

        this.saveChunkDebounce.put(chunk, Bukkit.getScheduler().scheduleSyncDelayedTask(OpenItems.getInstance(), saveRunnable, 10));
    }

    public void scanForWrongBlockModels(Chunk chunk) {

        var world = chunk.getWorld();
        var snapshot = chunk.getChunkSnapshot();

        var minHeight = world.getMinHeight();
        var maxHeight = world.getMaxHeight();

        var val = chunk.getPersistentDataContainer().get(
                OpenItems.CUSTOM_BLOCKS_CHECKED_CHUNK_KEY, PersistentDataType.BOOLEAN);

        if (val != null)
            return;

        OpenItems.async(() -> {
            var list = new ArrayList<int[]>();
            for (var x = 0; x < 16; x++)
                for (var z = 0; z < 16; z++)
                    for (var y = minHeight; y < maxHeight; y++) {
                        var blockType = snapshot.getBlockType(x, y, z);
                        if (blockType == Material.TRIPWIRE && OpenItemsConfig.replaceTripwiresOnChunkLoad)
                            list.add(new int[]{x, y, z});
                        if (blockType == Material.CHORUS_PLANT && OpenItemsConfig.replaceChorusPlantsOnChunkLoad)
                            list.add(new int[]{x, y, z});
                    }

            if (!list.isEmpty())
                OpenItems.sync(() -> {
                    for (var i : list) {
                        var block = chunk.getBlock(i[0], i[1], i[2]);

                        if (!this.placedBlocks.containsKey(block)) {
                            if (block.getBlockData() instanceof Tripwire tripwire) {
                                CustomTripwireModel.setDefaultId(tripwire);
                                block.setBlockData(tripwire, false);
                            } else if (block.getBlockData() instanceof MultipleFacing mf) {
                                CustomChorusModel.setDefaultId(mf);
                                block.setBlockData(mf, false);
                            }
                        }

                    }
                    chunk.getPersistentDataContainer().set(
                            OpenItems.CUSTOM_BLOCKS_CHECKED_CHUNK_KEY, PersistentDataType.BOOLEAN, true);
                });
        });

    }

    public void loadChunk(Chunk chunk) {
        var str = chunk.getPersistentDataContainer().get(
                OpenItems.CUSTOM_BLOCKS_CHUNK_KEY, PersistentDataType.STRING);

        if (str == null) {
            scanForWrongBlockModels(chunk);
            return;
        }
        OpenItems.async(() -> {
            var listType = new TypeToken<ArrayList<BlockLocationStore>>() {
            }.getType();

            List<BlockLocationStore> list = ConfigurationSerializableGsonAdapter.createGson().fromJson(str, listType);

            OpenItems.sync(() -> {
                var needSaveChunk = false;//remove invalid blocks and save chunk
                for (var i : list) {

                    if (!i.load()) {
                        needSaveChunk = true;
                        continue;
                    }

                    var block = chunk.getWorld().getBlockAt(i.getX(), i.getY(), i.getZ());

                    this.placedBlocks.put(block, i);
                    if (destroyOnLoad.containsKey(block)) {
                        var destroyRecord = destroyOnLoad.remove(block);
                        destroyBlock(block, destroyRecord.dropItem(), destroyRecord.setAir());
                        destroyRecord.callback().run();
                        continue;
                    }

                    var model = i.getCurrentModel();

                    if (model == null) {
                        OpenItems.getInstance().getLogger().severe(
                                "Custom block model not found: '" + i.getEffectiveBlockId() + "'. Removing stored block data.");
                        destroyBlock(block, false,false);
                        continue;
                    }

                    if (model.isReapplyOnLoadNeeded())
                        model.apply(block, false);

                    Bukkit.getPluginManager().callEvent(new CustomBlockLoadEvent(block, i));
                }
                if (needSaveChunk)
                    OpenItems.sync(() -> saveChunk(chunk));
                scanForWrongBlockModels(chunk);
            });
        });
    }

    public void destroyBlock(Block block, boolean dropItem, boolean setAir) {
        var placedBlock = this.placedBlocks.remove(block);

        if (placedBlock != null) {
            var model = placedBlock.getCurrentModel();
            if (model != null)
                model.remove(block);
        }

        // Always drop any leftover display entity for this block
        var display = this.displayEntities.remove(block);
        if (display != null && display.isValid())
            display.remove();

        if (setAir) {
            block.setType(Material.AIR);
            block.getState().update(true, true);
        }

        if (dropItem && placedBlock != null)
            block.getWorld().dropItem(block.getLocation().add(0.5, 0.1, 0.5), placedBlock.getItemToDrop());

        this.saveChunk(block.getChunk());
    }

    /**
     * Relocate custom blocks' registry entries and display entities after a piston push/pull.
     * Handles chains safely (remove-all, then put-all).
     *
     * @param fromTo      old location → new location
     * @param applyModels if true, teleport displays and re-apply models (call after piston animation)
     */
    public void moveBlocks(Map<Block, Block> fromTo, boolean applyModels) {
        if (fromTo.isEmpty())
            return;

        record PendingMove(Block from, Block to, BlockLocationStore store, ItemDisplay display) {
        }

        List<PendingMove> pending = new ArrayList<>();
        Set<Chunk> chunksToSave = new HashSet<>();

        for (var entry : fromTo.entrySet()) {
            var from = entry.getKey();
            var to = entry.getValue();
            var placedBlock = this.placedBlocks.remove(from);
            if (placedBlock == null)
                continue;

            var display = this.displayEntities.remove(from);
            pending.add(new PendingMove(from, to, placedBlock, display));
            chunksToSave.add(from.getChunk());
            chunksToSave.add(to.getChunk());
        }

        for (var move : pending) {
            move.store().setLocation(move.to().getX(), move.to().getY(), move.to().getZ());
            this.placedBlocks.put(move.to(), move.store());

            if (move.display() != null && move.display().isValid())
                this.displayEntities.put(move.to(), move.display());

            if (applyModels) {
                var model = move.store().getCurrentModel();
                if (model != null)
                    model.apply(move.to(), false);
            }
        }

        for (var chunk : chunksToSave)
            this.saveChunk(chunk);
    }

    /**
     * Relocate registry immediately, then sync displays/models after piston animation.
     */
    public void moveBlocks(Map<Block, Block> fromTo) {
        moveBlocks(fromTo, true);
    }

    /**
     * Relocate a custom block's registry entry and display entity after a piston push/pull.
     */
    public void moveBlock(Block from, Block to) {
        moveBlocks(Map.of(from, to), true);
    }

    /**
     * Re-apply models at the given blocks (e.g. after piston animation).
     */
    public void syncMovedBlocks(Collection<Block> destinations) {
        for (var to : destinations) {
            var placedBlock = this.placedBlocks.get(to);
            if (placedBlock == null)
                continue;

            var model = placedBlock.getCurrentModel();
            if (model != null)
                model.apply(to, false);
        }
    }

    /**
     * Destroy block if chunk is not loaded. If chunk is loaded - works like destroyBlock method
     *
     * @param block    block to break
     * @param dropItem drop block's item
     * @param setAir   - set block to air
     * @param callback - callback to run after block being destroyed
     */
    public void destroyBlockOnLoad(Block block, boolean dropItem, boolean setAir, Runnable callback) {
        if (block.getChunk().isLoaded() && getPlacedBlocks().containsKey(block)) {
            destroyBlock(block, dropItem, setAir);
            callback.run();
            return;
        }
        this.destroyOnLoad.put(block, new DestroyOnLoadBlock(dropItem, setAir, callback));
        block.getWorld().getChunkAtAsync(block).thenRun(() -> {
        });//just loading chunk. loadChunk(chunk) code will do the work
    }

    /**
     * Destroy block if chunk is not loaded. If chunk is loaded - works like destroyBlock method
     *
     * @param block    block to break
     * @param dropItem drop block's item
     * @param setAir   - set block to air
     */
    public void destroyBlockOnLoad(Block block, boolean dropItem, boolean setAir) {
        destroyBlockOnLoad(block, dropItem, setAir, () -> {
        });
    }

    /**
     * Place block from item in world
     *
     * @param block where to place custom block
     * @param item  item with custom block data
     * @return placed custom block's data
     */
    public BlockLocationStore placeBlock(Block block, ItemStack item) {
        var id = NBTUtil.getBlockId(item);

        var blocks = OpenItems.getInstance().getBlocks();

        item = item.clone();
        item.setAmount(1);
        item = NBTUtil.clearOverrideId(item);
        var placedBlock = new BlockLocationStore(block.getX(), block.getY(), block.getZ(), item);

        blocks.getPlacedBlocks().put(block, placedBlock);

        setBlockModel(block, id);

        blocks.saveChunk(block.getChunk());
        return placedBlock;
    }


    private void setBlockModel(Block block, String model) {
        var blockType = OpenItems.getInstance().getModelRegistry().getBlockTypes().get(model);
        blockType.apply(block, true);
    }

    /**
     * Set modelId for already placed block. This method will also will save custom chunk data.
     *
     * @param block block to apply modelId
     * @param modelId path to the block modelId
     */
    public void changeBlockModel(Block block, String modelId) {
        var placedBlock = this.placedBlocks.get(block);
        if (placedBlock == null)
            return;

        var previousModel = placedBlock.getCurrentModel();
        placedBlock.setOverrideId(null);
        placedBlock.setId(modelId);
        block.getState().update(true, false);
        if (previousModel != null)
            previousModel.remove(block);
        setBlockModel(block, modelId);
        this.saveChunk(block.getChunk());
    }

    /**
     * Promote a placed block to a different runtime model while keeping the real {@code id}
     * (e.g. slab half → double note block). The only path that sets {@code placement_id}.
     */
    public void overrideBlockModel(Block block, String overrideModelId) {
        var placedBlock = this.placedBlocks.get(block);
        if (placedBlock == null)
            return;

        if (overrideModelId.equals(placedBlock.getId())) {
            changeBlockModel(block, overrideModelId);
            return;
        }

        var previousModel = placedBlock.getCurrentModel();
        placedBlock.setOverrideId(overrideModelId);
        placedBlock.applyData();
        block.getState().update(true, false);
        if (previousModel != null)
            previousModel.remove(block);
        setBlockModel(block, overrideModelId);
        this.saveChunk(block.getChunk());
    }

    private static boolean isPromotedSlabWorldState(Block block) {
        if (block.getType() == Material.NOTE_BLOCK)
            return true;
        return block.getBlockData() instanceof Slab slab && slab.getType() == Slab.Type.DOUBLE;
    }


    /**
     * Remove from plugin registry all blocks from this chunk
     *
     * @param chunk chunk
     */
    public void cleanChunk(Chunk chunk) {
        for (var i : this.placedBlocks.entrySet()
                .stream().filter(i -> i.getKey().getChunk().equals(chunk)).toList()) {
            Bukkit.getPluginManager().callEvent(new CustomBlockUnloadEvent(i.getKey(), i.getValue()));
            var model = i.getValue().getCurrentModel();
            if (model != null)
                model.remove(i.getKey());
            this.placedBlocks.remove(i.getKey());
        }
    }

    /**
     * Remove all stairs/display ItemDisplay entities. Call on plugin disable
     * so they do not linger after reload/shutdown.
     */
    public void removeAllDisplayEntities() {
        for (var display : this.displayEntities.values()) {
            if (display != null && display.isValid())
                display.remove();
        }
        this.displayEntities.clear();

        // Orphans not in the map (e.g. after a crash mid-sync)
        for (var world : Bukkit.getWorlds()) {
            for (var entity : world.getEntitiesByClass(ItemDisplay.class)) {
                if (BlockEntityUtil.hasBlockDisplayTag(entity))
                    entity.remove();
            }
        }
    }

    public BlockBreakSpeedModifiers getBlockBreakSpeedModifiers() {
        return blockBreakSpeedModifiers;
    }
}

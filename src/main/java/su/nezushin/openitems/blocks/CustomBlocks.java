package su.nezushin.openitems.blocks;

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
import su.nezushin.openitems.blocks.storage.chunk.ChunkBlockSnapshotDecoder;
import su.nezushin.openitems.blocks.storage.chunk.ChunkBlockSnapshotEncoder;
import su.nezushin.openitems.blocks.storage.chunk.ChunkBlockSnapshotSerializer;
import su.nezushin.openitems.blocks.storage.chunk.ChunkBlockStoreFormat;
import su.nezushin.openitems.blocks.storage.pdc.ChunkBlockPdcReader;
import su.nezushin.openitems.blocks.storage.pdc.ChunkBlockPdcWriter;
import su.nezushin.openitems.blocks.types.CustomChorusModel;
import su.nezushin.openitems.blocks.types.CustomTripwireModel;
import su.nezushin.openitems.events.CustomBlockLoadEvent;
import su.nezushin.openitems.events.CustomBlockUnloadEvent;
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

    /**
     * Blocks registered while a resource pack build had cleared the model registry.
     * They stay in {@link #placedBlocks} so interactions work; the model is resolved when the registry is full.
     */
    private final Set<Block> blocksAwaitingModel = new HashSet<>();

    private BlockBreakSpeedModifiers blockBreakSpeedModifiers;

    private final ChunkBlockSnapshotEncoder snapshotEncoder = new ChunkBlockSnapshotEncoder();
    private final ChunkBlockSnapshotDecoder snapshotDecoder = new ChunkBlockSnapshotDecoder();
    private final ChunkBlockSnapshotSerializer snapshotSerializer = new ChunkBlockSnapshotSerializer();
    private final ChunkBlockPdcWriter pdcWriter = new ChunkBlockPdcWriter();
    private final ChunkBlockPdcReader pdcReader = new ChunkBlockPdcReader();

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

            for (BlockLocationStore store : list)
                store.applyData();

            OpenItems.async(() -> {
                if (list.isEmpty()) {
                    OpenItems.sync(() -> pdcWriter.clear(chunk));
                    return;
                }

                var snapshot = snapshotEncoder.encode(chunk, list);
                byte[] rawPayload = snapshotSerializer.encode(snapshot);
                OpenItems.sync(() -> pdcWriter.write(chunk, rawPayload));
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
        var format = pdcReader.detect(chunk);

        if (format == ChunkBlockStoreFormat.EMPTY) {
            scanForWrongBlockModels(chunk);
            return;
        }

        OpenItems.async(() -> {
            byte[] rawPayload = pdcReader.readRaw(chunk);
            List<BlockLocationStore> list = snapshotDecoder.decode(chunk, snapshotSerializer.decode(rawPayload));

            OpenItems.sync(() -> {
                var refreshing = OpenItems.getInstance().getResourcePackBuilder().isRegistryRefreshing();
                var needSaveChunk = false;
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

                    if (refreshing)
                        blocksAwaitingModel.add(block);
                    else
                        resolveBlockModel(block, i);

                    Bukkit.getPluginManager().callEvent(new CustomBlockLoadEvent(block, i));
                }
                if (needSaveChunk)
                    saveChunk(chunk);
                scanForWrongBlockModels(chunk);
            });
        });
    }

    /**
     * Resolve models for blocks that were registered while the registry was empty.
     * A model that still matches the world block is left as-is, except display models that must be reapplied.
     * A model that no longer matches is applied. A missing model is removed, same as a normal load.
     * Main thread only.
     */
    public void finishBlocksLoadedDuringRefresh() {
        var blocks = new ArrayList<>(blocksAwaitingModel);
        blocksAwaitingModel.clear();
        for (var block : blocks) {
            if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4))
                continue;

            var store = this.placedBlocks.get(block);
            if (store == null)
                continue;

            resolveBlockModel(block, store);
        }
    }

    private void resolveBlockModel(Block block, BlockLocationStore store) {
        var model = store.getCurrentModel();

        if (model == null) {
            OpenItems.getInstance().getLogger().severe(
                    "Custom block model not found: '" + store.getEffectiveBlockId() + "'. Removing stored block data.");
            destroyBlock(block, false, false);
            return;
        }

        if (model.isReapplyOnLoadNeeded())
            model.apply(block, false);
    }

    public void destroyBlock(Block block, boolean dropItem, boolean setAir) {
        blocksAwaitingModel.remove(block);
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

    /**
     * Register a block placed by WorldEdit after the host block state is already set.
     */
    public void registerWorldEditBlock(Block block, ItemStack item) {
        registerWorldEditBlockMetadata(block, item);
        applyWorldEditModel(block);
    }

    /**
     * Insert/update registry entry for a WorldEdit placement without applying visuals yet.
     */
    public BlockLocationStore registerWorldEditBlockMetadata(Block block, ItemStack item) {
        item = item.clone();
        item.setAmount(1);
        item = NBTUtil.clearOverrideId(item);
        var placedBlock = new BlockLocationStore(block.getX(), block.getY(), block.getZ(), item);
        placedBlocks.put(block, placedBlock);
        saveChunk(block.getChunk());
        return placedBlock;
    }

    /**
     * Apply the custom model/display for a block already registered via WorldEdit.
     */
    public void applyWorldEditModel(Block block) {
        var placedBlock = placedBlocks.get(block);
        if (placedBlock == null)
            return;

        var model = placedBlock.getCurrentModel();
        if (model != null)
            model.apply(block, false);
    }


    private void setBlockModel(Block block, String model) {
        var blockType = OpenItems.getInstance().getModelRegistry().getBlockTypes().get(model);
        blockType.apply(block, true);
    }

    /**
     * Set modelId for already placed block. This method will also will save custom chunk data.
     *
     * @param block   block to apply modelId
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
        blocksAwaitingModel.removeIf(block -> block.getWorld().equals(chunk.getWorld())
                && (block.getX() >> 4) == chunk.getX()
                && (block.getZ() >> 4) == chunk.getZ());
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

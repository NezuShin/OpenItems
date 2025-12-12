package su.nezushin.openitems.blocks;

import com.google.common.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.storage.BlockDataStore;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;
import su.nezushin.openitems.blocks.types.CustomChorusModel;
import su.nezushin.openitems.blocks.types.CustomTripwireModel;
import su.nezushin.openitems.events.CustomBlockLoadEvent;
import su.nezushin.openitems.events.CustomBlockUnloadEvent;
import su.nezushin.openitems.gson.ConfigurationSerializableGsonAdapter;
import su.nezushin.openitems.utils.NBTUtil;
import su.nezushin.openitems.utils.OpenItemsConfig;

import java.util.*;

public class CustomBlocks {

    //All loaded blocks in server
    private Map<Block, BlockLocationStore> placedBlocks = new HashMap<>();

    //Blocks need to be destroyed on next chunk load
    private Map<Block, DestroyOnLoadBlock> destroyOnLoad = new HashMap<>();

    private BlockBreakSpeedModifiers blockBreakSpeedModifiers;


    private record DestroyOnLoadBlock(boolean dropItem, boolean setAir, Runnable callback) {

    }

    public CustomBlocks() {
        Bukkit.getPluginManager().registerEvents(new CustomBlocksListener(), OpenItems.getInstance());

        blockBreakSpeedModifiers = new BlockBreakSpeedModifiers();

        for (var world : Bukkit.getWorlds())
            for (var chunk : world.getLoadedChunks())
                loadChunk(chunk);
    }

    public Map<Block, BlockLocationStore> getPlacedBlocks() {
        return placedBlocks;
    }



    public void saveChunk(Chunk chunk) {
        List<BlockDataStore> list = new ArrayList<>();

        for (var i : this.placedBlocks.entrySet()
                .stream().filter(i -> i.getKey().getChunk().equals(chunk)).toList()) {

            list.add(i.getValue());
        }
        OpenItems.async(() -> {
            var str = ConfigurationSerializableGsonAdapter.createGson().toJson(list);
            OpenItems.sync(() -> {
                chunk.getPersistentDataContainer().set(OpenItems.CUSTOM_BLOCKS_CHUNK_KEY, PersistentDataType.STRING, str);
            });
        });
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

                        return;
                    }


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
        if (setAir) {
            block.setType(Material.AIR);
            block.getState().update(true, true);
        }

        if (dropItem)
            block.getWorld().dropItem(block.getLocation().add(0.5, 0.1, 0.5), placedBlock.getItemToDrop());

        this.saveChunk(block.getChunk());
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
        var placedBlock = new BlockLocationStore(block.getX(), block.getY(), block.getZ(), item);

        setBlockModel(block, id);

        blocks.getPlacedBlocks().put(block, placedBlock);

        blocks.saveChunk(block.getChunk());
        return placedBlock;
    }


    private void setBlockModel(Block block, String model) {
        var blockType = OpenItems.getInstance().getModelRegistry().getBlockTypes().get(model);
        blockType.apply(block);
    }

    /**
     * Set model for already placed block. This method will also will save custom chunk data.
     *
     * @param block block to apply model
     * @param model path to the block model
     */
    public void changeBlockModel(Block block, String model) {
        var placedBlock = this.placedBlocks.get(block);
        placedBlock.setId(model);
        block.getState().update(true, false);
        setBlockModel(block, model);
        this.saveChunk(block.getChunk());
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
            this.placedBlocks.remove(i.getKey());
        }
    }

    public BlockBreakSpeedModifiers getBlockBreakSpeedModifiers() {
        return blockBreakSpeedModifiers;
    }
}

package su.nezushin.openitems.blocks;

import com.google.common.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.persistence.PersistentDataType;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.storage.BlockDataStore;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;
import su.nezushin.openitems.blocks.types.CustomTripwireModel;
import su.nezushin.openitems.events.CustomBlockLoadEvent;
import su.nezushin.openitems.events.CustomBlockUnloadEvent;
import su.nezushin.openitems.gson.ConfigurationSerializableGsonAdapter;
import su.nezushin.openitems.utils.OpenItemsConfig;

import java.util.*;

public class CustomBlocks {

    //All loaded blocks in server
    private Map<Block, BlockLocationStore> placedBlocks = new HashMap<>();

    public CustomBlocks() {
        Bukkit.getPluginManager().registerEvents(new CustomBlocksListener(), OpenItems.getInstance());

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


            Bukkit.getPluginManager().callEvent(new CustomBlockUnloadEvent(i.getKey(), i.getValue()));
            list.add(i.getValue());
        }
        OpenItems.async(() -> {
            var str = ConfigurationSerializableGsonAdapter.createGson().toJson(list);
            OpenItems.sync(() -> {
                chunk.getPersistentDataContainer().set(OpenItems.CUSTOM_BLOCKS_KEY, PersistentDataType.STRING, str);
            });
        });
    }

    public void scanForWrongTripwires(Chunk chunk) {
        if (!OpenItemsConfig.replaceTripwiresOnChunkLoad)
            return;

        var world = chunk.getWorld();
        var snapshot = chunk.getChunkSnapshot();

        var minHeight = world.getMinHeight();
        var maxHeight = world.getMaxHeight();

        OpenItems.async(() -> {
            var list = new ArrayList<int[]>();
            //snapshot.getBlockType()
            for (var x = 0; x < 16; x++)
                for (var z = 0; z < 16; z++)
                    for (var y = minHeight; y < maxHeight; y++)
                        if (snapshot.getBlockType(x, y, z) == Material.TRIPWIRE) {
                            list.add(new int[]{x, y, z});
                        }

            if (!list.isEmpty())
                OpenItems.sync(() -> {
                    for (var i : list) {
                        var block = world.getBlockAt(i[0], i[1], i[2]);
                        if (!this.placedBlocks.containsKey(block) && block.getBlockData() instanceof Tripwire tripwire) {
                            CustomTripwireModel.setDefaultId(tripwire);
                            block.setBlockData(tripwire, false);
                        }

                    }
                });
        });

    }

    public void loadChunk(Chunk chunk) {
        var str = chunk.getPersistentDataContainer().get(
                OpenItems.CUSTOM_BLOCKS_KEY, PersistentDataType.STRING);

        if (str == null)
            return;
        OpenItems.async(() -> {
            var listType = new TypeToken<ArrayList<BlockLocationStore>>() {
            }.getType();

            List<BlockLocationStore> list = ConfigurationSerializableGsonAdapter.createGson().fromJson(str, listType);
            OpenItems.sync(() -> {
                for (var i : list) {

                    if (!i.load()) {
                        OpenItems.sync(() -> {//remove invalid blocks and save chunk
                            saveChunk(chunk);
                        });
                        continue;
                    }

                    var block = chunk.getWorld().getBlockAt(i.getX(), i.getY(), i.getZ());

                    this.placedBlocks.put(block, i);
                    Bukkit.getPluginManager().callEvent(new CustomBlockLoadEvent(block, i));
                }
                scanForWrongTripwires(chunk);
            });
        });
    }

    public void destroyBlock(Block block, boolean dropItem, boolean setAir) {
        var placedBlock = this.placedBlocks.remove(block);
        if (setAir)
            block.setType(Material.AIR);

        if (placedBlock.dropOnDestroy() && dropItem) {
            block.getWorld().dropItem(block.getLocation().add(0.5, 0.1, 0.5), placedBlock.getItemToDrop());
        }
        this.saveChunk(block.getChunk());

    }

    public void cleanChunk(Chunk chunk) {
        for (var i : this.placedBlocks.entrySet()
                .stream().filter(i -> i.getKey().getChunk().equals(chunk)).toList()) {
            this.placedBlocks.remove(i.getKey());
        }
    }
}

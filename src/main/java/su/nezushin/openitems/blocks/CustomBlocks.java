package su.nezushin.openitems.blocks;

import com.google.common.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.gson.ItemStackGsonAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomBlocks {

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
            list.add(i.getValue());
        }
        OpenItems.async(() -> {
            var str = ItemStackGsonAdapter.createGson().toJson(list);
            OpenItems.sync(() -> {
                chunk.getPersistentDataContainer().set(OpenItems.CUSTOM_BLOCKS_KEY, PersistentDataType.STRING, str);
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

            List<BlockLocationStore> list = ItemStackGsonAdapter.createGson().fromJson(str, listType);
            OpenItems.sync(() -> {
                for (var i : list) {
                    if (!i.load()) {
                        OpenItems.sync(() -> {
                            saveChunk(chunk);
                        });
                        continue;
                    }

                    this.placedBlocks.put(chunk.getWorld().getBlockAt(i.getX(), i.getY(), i.getZ()), i);
                }
            });
        });
    }

    public void removeBlock(Block block) {
        this.placedBlocks.remove(block);
        block.setType(Material.AIR);
        this.saveChunk(block.getChunk());
    }

    public void destroyBlock(Block block) {
        var placedBlock = this.placedBlocks.remove(block);
        block.setType(Material.AIR);
        if (placedBlock.dropOnDestroy())
            block.getWorld().dropItem(block.getLocation().add(0.5, 0.1, 0.5), placedBlock.getItemToDrop());
        this.saveChunk(block.getChunk());

    }

    public void cleanChunk(Chunk chunk) {
        for (var i : this.placedBlocks.entrySet()
                .stream().filter(i -> i.getKey().getChunk().equals(chunk)).toList()) {
            this.placedBlocks.remove(i.getKey());
        }
    }
}

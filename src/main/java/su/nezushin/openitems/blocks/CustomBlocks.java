package su.nezushin.openitems.blocks;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Instrument;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.types.CustomBlockType;
import su.nezushin.openitems.blocks.types.CustomNoteblockType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomBlocks {

    private Map<Block, String> placedBlocks = new HashMap<>();


    public CustomBlocks() {
        //blockTypes.put("ns:teleporter", new CustomNoteblockType(CustomNoteblockType.getId(Instrument.BASS_DRUM, 0, false)));
        //blockTypes.put("ns:teleporter", new CustomNoteblockType(CustomNoteblockType.getId(Instrument.PIANO, 0, true)));

        Bukkit.getPluginManager().registerEvents(new CustomBlocksListener(), OpenItems.getInstance());

        for (var world : Bukkit.getWorlds())
            for (var chunk : world.getLoadedChunks())
                loadChunk(chunk);
    }

    public Map<Block, String> getPlacedBlocks() {
        return placedBlocks;
    }

    public void saveChunk(Chunk chunk) {
        List<BlockStore> list = new ArrayList<>();

        for (var i : this.placedBlocks.entrySet()
                .stream().filter(i -> i.getKey().getChunk().equals(chunk)).toList()) {
            list.add(new BlockStore(i.getValue(), i.getKey().getX(), i.getKey().getY(), i.getKey().getZ()));
        }
        OpenItems.async(() -> {
            var str = new Gson().toJson(list);
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
            var listType = new TypeToken<ArrayList<BlockStore>>() {
            }.getType();

            List<BlockStore> list = new Gson().fromJson(str, listType);
            OpenItems.sync(() -> {
                for (var i : list)
                    this.placedBlocks.put(chunk.getWorld().getBlockAt(i.getX(), i.getY(), i.getZ()), i.getId());
            });
        });
    }

    public void removeBlock(Block block) {
        this.placedBlocks.remove(block);
        this.saveChunk(block.getChunk());
    }

    public void cleanChunk(Chunk chunk) {
        for (var i : this.placedBlocks.entrySet()
                .stream().filter(i -> i.getKey().getChunk().equals(chunk)).toList()) {
            this.placedBlocks.remove(i.getKey());
        }
    }
}

package su.nezushin.openitems.blocks;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockVector;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.types.CustomBlockType;
import su.nezushin.openitems.blocks.types.CustomNoteblockType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomBlocks {

    private Map<Block, String> placedBlocks = new HashMap<>();

    private Map<String, CustomBlockType> blockTypes = new HashMap<>();


    public CustomBlocks() {
        //blockTypes.put("ns:teleporter", new CustomNoteblockType(CustomNoteblockType.getId(Instrument.BASS_DRUM, 0, false)));
        blockTypes.put("ns:teleporter", new CustomNoteblockType(CustomNoteblockType.getId(Instrument.PIANO, 0, true)));

        Bukkit.getPluginManager().registerEvents(new CustomBlocksListener(), OpenItems.getInstance());

        for(var world : Bukkit.getWorlds())
            for(var chunk : world.getLoadedChunks())
                loadChunk(chunk);
    }

    public Map<Block, String> getPlacedBlocks() {
        return placedBlocks;
    }

    public Map<String, CustomBlockType> getBlockTypes() {
        return blockTypes;
    }

    public void saveChunk(Chunk chunk) {
        List<BlockStore> list = new ArrayList<>();

        for (var i : OpenItems.getInstance().getBlocks().getPlacedBlocks().entrySet()
                .stream().filter(i -> i.getKey().getChunk().equals(chunk)).toList()) {
            list.add(new BlockStore(i.getValue(), i.getKey().getX(), i.getKey().getY(), i.getKey().getZ()));
            //OpenItems.getInstance().getBlocks().getPlacedBlocks().remove(i.getKey());
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
                    OpenItems.getInstance().getBlocks().getPlacedBlocks().put(chunk.getWorld().getBlockAt(i.getX(), i.getY(), i.getZ()), i.getId());
            });
        });
    }


    public static String getId(Block block) {
        var metadata = block.getMetadata("ns:custom_block_id");

        if (metadata.isEmpty())
            return null;

        return metadata.getFirst().asString();
    }

    public static void setId(Block block, String id) {
        block.setMetadata("ns:custom_block_id", new FixedMetadataValue(OpenItems.getInstance(), id));
    }

}

package su.nezushin.openitems.blocks;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockVector;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CustomBlocksListener implements Listener {


    @EventHandler
    public void chunkLoad(ChunkLoadEvent e) {
        var chunk = e.getChunk();
        OpenItems.getInstance().getBlocks().loadChunk(chunk);
    }

    @EventHandler
    public void blockPhysics(BlockPhysicsEvent e) {
        var block = e.getBlock();
        var blocks = OpenItems.getInstance().getBlocks();

        if (blocks.getPlacedBlocks().containsKey(block)) {
            e.setCancelled(true);
            return;
        }
        for (var face : Utils.getBlockFacesForChorus())
            if (blocks.getPlacedBlocks().containsKey(block.getRelative(face))) {
                e.setCancelled(true);
                return;
            }
    }

    @EventHandler
    public void breakBlock(BlockBreakEvent e) {
        var block = e.getBlock();
        var blocks = OpenItems.getInstance().getBlocks();

        if (!blocks.getPlacedBlocks().containsKey(block))
            return;

        blocks.getPlacedBlocks().remove(block);
        blocks.saveChunk(block.getChunk());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void placeBlock(BlockPlaceEvent e) {
        var item = e.getItemInHand();
        var block = e.getBlock();
        if (item == null || item.getType().isAir())
            return;

        var id = BlockNBTUtil.getBlockId(item);

        if (id == null)
            return;
        var blocks = OpenItems.getInstance().getBlocks();

        var blockType = blocks.getBlockTypes().get(id);

        if (blockType == null)
            return;

        blockType.apply(block);

        blocks.getPlacedBlocks().put(block, id);
        blocks.saveChunk(block.getChunk());
    }


    @EventHandler
    public void click(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        var blocks = OpenItems.getInstance().getBlocks();
        if (block == null)
            return;
        if (!blocks.getPlacedBlocks().containsKey(block))
            return;
        if (!player.isSneaking())
            e.setUseInteractedBlock(Event.Result.DENY);
    }
}

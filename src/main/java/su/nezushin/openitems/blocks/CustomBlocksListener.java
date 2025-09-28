package su.nezushin.openitems.blocks;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.types.CustomBlockType;
import su.nezushin.openitems.blocks.types.CustomTripwireType;
import su.nezushin.openitems.utils.Utils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CustomBlocksListener implements Listener {


    @EventHandler
    public void chunkLoad(ChunkLoadEvent e) {
        var chunk = e.getChunk();
        OpenItems.getInstance().getBlocks().loadChunk(chunk);
    }

    @EventHandler
    public void chunkUnload(ChunkUnloadEvent e) {
        var chunk = e.getChunk();
        OpenItems.getInstance().getBlocks().cleanChunk(chunk);
    }

    @EventHandler
    public void explode(BlockExplodeEvent e) {
        e.blockList().removeIf(i -> {
            var blockId = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(i);
            if (blockId == null) return false;
            var blockType = OpenItems.getInstance().getModelRegistry().getBlockTypes().get(blockId);
            if (blockType == null) return false;
            if (blockType.canBeBlown()) {
                OpenItems.getInstance().getBlocks().getPlacedBlocks().remove(i);

                i.setType(Material.AIR);
                e.blockList().remove(i);
                return false;
            }
            return true;
        });
    }

    @EventHandler
    public void explode(EntityExplodeEvent e) {
        e.blockList().removeIf(i -> {
            var blockId = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(i);
            if (blockId == null) return false;
            var blockType = OpenItems.getInstance().getModelRegistry().getBlockTypes().get(blockId);
            if (blockType == null) return false;
            if (blockType.canBeBlown()) {
                OpenItems.getInstance().getBlocks().getPlacedBlocks().remove(i);
                i.setType(Material.AIR);
                e.blockList().remove(i);
                return false;
            }
            return true;
        });
    }

    @EventHandler
    public void burn(BlockBurnEvent e) {
        var blockId = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(e.getBlock());
        if (blockId == null) return;
        var blockType = OpenItems.getInstance().getModelRegistry().getBlockTypes().get(blockId);
        if (blockType == null) return;
        if (blockType.canBurn()) {
            OpenItems.getInstance().getBlocks().getPlacedBlocks().remove(e.getBlock());
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void breakBlock(BlockBreakBlockEvent e) {

    }

    @EventHandler
    public void entityChange(EntityChangeBlockEvent e) {
        var blockId = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(e.getBlock());
        if (blockId == null) return;
        var blockType = OpenItems.getInstance().getModelRegistry().getBlockTypes().get(blockId);
        if (blockType == null) return;
        if (blockType.canBeReplaced()) {
            OpenItems.getInstance().getBlocks().getPlacedBlocks().remove(e.getBlock());
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void structureGrow(StructureGrowEvent e) {
        e.getBlocks().removeIf(i -> {
            var blockId = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(i.getBlock());
            if (blockId == null) return false;
            var blockType = OpenItems.getInstance().getModelRegistry().getBlockTypes().get(blockId);
            if (blockType == null) return false;
            if (blockType.canBeReplaced()) {
                OpenItems.getInstance().getBlocks().getPlacedBlocks().remove(i);
                return false;
            }
            return true;
        });

    }

    @EventHandler
    public void blockFromTo(BlockFromToEvent e) {
        System.out.println(e.getBlock());
        System.out.println(e.getToBlock());
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void blockPhysics(BlockPhysicsEvent e) {
        var block = e.getBlock();


        var blocks = OpenItems.getInstance().getBlocks();
        if (blocks.getPlacedBlocks().containsKey(block)) {
            e.setCancelled(true);
            var blockType = OpenItems.getInstance().getModelRegistry().getBlockTypes()
                    .get(blocks.getPlacedBlocks().get(block));

            if (blockType != null && blockType.applyOnPhysics()) {
                var blockData = e.getChangedBlockData();
                blockType.apply(blockData);
                block.setBlockData(blockData);
            }
            return;
        } else if (block.getType().equals(Material.TRIPWIRE) && e.getChangedBlockData() instanceof Tripwire blockData) {
            CustomTripwireType.setDefaultId(blockData);
            block.setBlockData(blockData);
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

        if (!blocks.getPlacedBlocks().containsKey(block)) return;
        e.setDropItems(false);

        blocks.removeBlock(block);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void placeBlock(BlockPlaceEvent e) {
        var item = e.getItemInHand();
        var block = e.getBlock();
        if (item == null || item.getType().isAir()) return;

        var id = BlockNBTUtil.getBlockId(item);

        if (id == null) return;
        var blocks = OpenItems.getInstance().getBlocks();

        var blockType = OpenItems.getInstance().getModelRegistry().getBlockTypes().get(id);

        if (blockType == null) return;

        blockType.apply(block);

        blocks.getPlacedBlocks().put(block, id);
        blocks.saveChunk(block.getChunk());
    }


    @EventHandler
    public void click(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        var blocks = OpenItems.getInstance().getBlocks();
        if (block == null) return;
        if (!blocks.getPlacedBlocks().containsKey(block)) return;
        if (!player.isSneaking()) e.setUseInteractedBlock(Event.Result.DENY);
    }
}

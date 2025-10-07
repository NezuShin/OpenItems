package su.nezushin.openitems.blocks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;
import su.nezushin.openitems.blocks.types.CustomChorusModel;
import su.nezushin.openitems.events.*;
import su.nezushin.openitems.utils.NBTUtil;
import su.nezushin.openitems.blocks.types.CustomTripwireModel;
import su.nezushin.openitems.utils.OpenItemsConfig;
import su.nezushin.openitems.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class CustomBlocksListener implements Listener {

    private Map<Block, BlockLocationStore> brokenBlocks = new HashMap<>();

    @EventHandler
    public void breakBlock(BlockBreakEvent e) {
        var block = e.getBlock();
        var blocks = OpenItems.getInstance().getBlocks();

        var placedBlock = blocks.getPlacedBlocks().get(block);

        if (placedBlock == null) return;

        //e.setDropItems(false);
        var event = new CustomBlockBreakEvent(block, placedBlock, e);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled() || e.isCancelled()) {
            e.setCancelled(true);
            return;
        }
        if (e.getPlayer().getGameMode() != GameMode.CREATIVE)
            brokenBlocks.put(block, placedBlock);
        OpenItems.getInstance().getBlocks().destroyBlock(e.getBlock(), false, false);
    }

    //Compatibility with other Bukkit plugins
    @EventHandler(priority = EventPriority.LOWEST)
    public void blockDropItemEvent(BlockDropItemEvent e) {
        var block = e.getBlock();
        var placedBlock = brokenBlocks.remove(block);

        if (placedBlock == null)
            return;

        e.getItems().clear();

        if (placedBlock.dropOnBreak()) {
            var item = block.getWorld().dropItem(block.getLocation().add(0.5, 0.1, 0.5), placedBlock.getItemToDrop());

            e.getItems().add(item);
        }

        var event = new CustomBlockDropItemEvent(block, placedBlock, e);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            e.setCancelled(true);

    }

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
            var placedBlock = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(i);
            if (placedBlock == null) return false;
            if (placedBlock.canBeBlown()) {

                var event = new CustomBlockExplodeEvent(i, placedBlock, e);

                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled())
                    return true;

                OpenItems.getInstance().getBlocks().destroyBlock(i, placedBlock.dropOnExplosion()
                        && Math.random() < e.getYield(), true);

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
            var placedBlock = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(i);
            if (placedBlock == null) return false;
            if (placedBlock.canBeBlown()) {

                var event = new CustomBlockExplodeEvent(i, placedBlock, e);

                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled())
                    return true;


                OpenItems.getInstance().getBlocks().destroyBlock(i, placedBlock.dropOnExplosion()
                        && Math.random() < e.getYield(), true);

                i.setType(Material.AIR);
                e.blockList().remove(i);
                return false;
            }
            return true;
        });
    }

    @EventHandler
    public void burn(BlockBurnEvent e) {
        var placedBlock = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(e.getBlock());
        if (placedBlock == null) return;
        if (placedBlock.canBurn()) {
            var event = new CustomBlockBurnEvent(e.getBlock(), placedBlock, e);

            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                e.setCancelled(true);
                return;
            }

            OpenItems.getInstance().getBlocks().destroyBlock(e.getBlock(), placedBlock.dropOnBurn(), true);
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void entityChange(EntityChangeBlockEvent e) {
        var placedBlock = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(e.getBlock());
        if (placedBlock == null) return;
        if (placedBlock.canBeReplaced()) {
            OpenItems.getInstance().getBlocks().destroyBlock(e.getBlock(), false, true);
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void blockFromTo(BlockFromToEvent e) {
        var block = e.getToBlock();
        if (!OpenItemsConfig.enableTripwires)
            return;
        if (!block.getType().equals(Material.TRIPWIRE))
            return;

        var blocks = OpenItems.getInstance().getBlocks();

        var placedBlock = blocks.getPlacedBlocks().get(block);

        if (placedBlock == null)
            return;

        if (placedBlock.canBeDestroyedByLiquid()) {
            blocks.destroyBlock(block, placedBlock.dropOnDestroyByLiquid(), true);
            e.setCancelled(true);
            block.getState().update(true, true);
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void chorusGrow(BlockSpreadEvent e) {
        var block = e.getSource();
        if (!OpenItemsConfig.enableChorus)
            return;
        if (!block.getType().equals(Material.CHORUS_FLOWER))
            return;
        OpenItems.sync(() -> {
            if (!(block.getBlockData() instanceof MultipleFacing multipleFacing))
                return;

            CustomChorusModel.setDefaultId(multipleFacing);
            block.setBlockData(multipleFacing);
        });
    }

    @EventHandler
    public void chorusGrow(BlockGrowEvent e) {
        var block = e.getBlock();
        var newState = e.getNewState();

        if (!newState.getType().equals(Material.CHORUS_FLOWER))
            return;

        var blocks = OpenItems.getInstance().getBlocks();

        for (var face : Utils.getMainBlockFaces()) {
            var b = block.getRelative(face);

            if (!b.getType().equals(Material.CHORUS_PLANT))
                continue;

            if (!(b.getBlockData() instanceof MultipleFacing mf))
                continue;

            var placedBlock = blocks.getPlacedBlocks().get(b);

            if (placedBlock != null) {
                placedBlock.getModel().apply(mf);
            } else {
                CustomChorusModel.setDefaultId(mf);
            }
            b.setBlockData(mf, false);

        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void blockPhysics(BlockPhysicsEvent e) {
        var block = e.getBlock();


        var blocks = OpenItems.getInstance().getBlocks();
        if (blocks.getPlacedBlocks().containsKey(block)) {
            e.setCancelled(true);
            var blockType = OpenItems.getInstance().getModelRegistry().getBlockTypes()
                    .get(blocks.getPlacedBlocks().get(block).getId());

            if (blockType != null && blockType.applyOnPhysics()) {
                var blockData = e.getChangedBlockData();
                blockType.apply(blockData);
                block.setBlockData(blockData, false);
            }
            return;
        } else if (block.getType().equals(Material.TRIPWIRE) && OpenItemsConfig.enableTripwires
                && e.getChangedBlockData() instanceof Tripwire blockData) {
            CustomTripwireModel.setDefaultId(blockData);
            block.setBlockData(blockData, false);
        } else if (block.getType().equals(Material.CHORUS_PLANT) && OpenItemsConfig.enableChorus
                && e.getChangedBlockData() instanceof MultipleFacing blockData) {
            CustomChorusModel.setDefaultId(blockData);
            block.setBlockData(blockData, false);
        }
        for (var face : Utils.getMainBlockFaces())
            if (blocks.getPlacedBlocks().containsKey(block.getRelative(face))) {
                e.setCancelled(true);
                return;
            }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void placeBlock(BlockPlaceEvent e) {
        var item = e.getItemInHand();
        var block = e.getBlock();
        if (item == null || item.getType().isAir()) return;

        var id = NBTUtil.getBlockId(item);

        if (id == null) return;
        var blocks = OpenItems.getInstance().getBlocks();

        var blockType = OpenItems.getInstance().getModelRegistry().getBlockTypes().get(id);

        if (blockType == null) return;

        item = item.clone();
        item.setAmount(1);
        var placedBlock = new BlockLocationStore(block.getX(), block.getY(), block.getZ(), item);


        var event = new CustomBlockPlaceEvent(block, placedBlock, e);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            e.setCancelled(true);
            return;
        }

        blockType.apply(block);


        blocks.getPlacedBlocks().put(block, placedBlock);
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

    @EventHandler
    public void note(NotePlayEvent e) {
        e.setCancelled(true);
    }
}

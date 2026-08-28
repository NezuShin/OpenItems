package su.nezushin.openitems.blocks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;
import su.nezushin.openitems.blocks.types.CustomBlockModel;
import su.nezushin.openitems.blocks.types.CustomChorusModel;
import su.nezushin.openitems.blocks.types.CustomNoteblockModel;
import su.nezushin.openitems.events.*;
import su.nezushin.openitems.utils.BlockEntityUtil;
import su.nezushin.openitems.utils.NBTUtil;
import su.nezushin.openitems.blocks.types.CustomTripwireModel;
import su.nezushin.openitems.utils.OpenItemsConfig;
import su.nezushin.openitems.utils.Utils;

import java.util.*;

public class CustomBlocksListener implements Listener {

    private Map<Block, BlockLocationStore> brokenBlocks = new HashMap<>();

    /** Locations where we intentionally destroyed custom chorus; suppress vanilla fruit briefly. */
    private final Map<Block, Integer> suppressChorusFruitUntilTick = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void breakBlock(BlockBreakEvent e) {
        var block = e.getBlock();
        var blocks = OpenItems.getInstance().getBlocks();

        var placedBlock = blocks.getPlacedBlocks().get(block);

        var player = e.getPlayer();

        if (placedBlock == null) return;

        //e.setDropItems(false);
        var event = new CustomBlockBreakEvent(block, placedBlock, e);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled() || e.isCancelled()) {
            e.setCancelled(true);
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            brokenBlocks.put(block, placedBlock);
        }
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
    public void entitiesLoad(EntitiesLoadEvent e) {
        for (var entity : e.getEntities()) {
            if (BlockEntityUtil.hasBlockDisplayTag(entity))
                entity.remove();
        }
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
                return true;
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
                return true;
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

    @EventHandler(ignoreCancelled = true)
    public void pistonExtend(BlockPistonExtendEvent e) {
        if (!handlePistonFragile(e.getBlocks(), e.getBlock(), pistonFacing(e.getBlock()))) {
            e.setCancelled(true);
            return;
        }
        // getDirection() = direction blocks move
        handlePistonMove(e.getBlocks(), e.getDirection());
    }

    @EventHandler(ignoreCancelled = true)
    public void pistonRetract(BlockPistonRetractEvent e) {
        if (!handlePistonFragile(e.getBlocks(), e.getBlock(), pistonFacing(e.getBlock()))) {
            e.setCancelled(true);
            return;
        }
        // Sticky retract already reports pull direction in getDirection() (opposite of facing)
        handlePistonMove(e.getBlocks(), e.getDirection());
    }

    private BlockFace pistonFacing(Block piston) {
        if (piston.getBlockData() instanceof Directional directional)
            return directional.getFacing();
        return BlockFace.SELF;
    }

    /**
     * Chorus / tripwire are broken by pistons (DESTROY reaction), not pushed.
     *
     * @param facing piston block facing (not event movement direction)
     * @return false if the piston should be cancelled ({@code can_be_replaced=false})
     */
    private boolean handlePistonFragile(List<Block> movedBlocks, Block piston, BlockFace facing) {
        var blocks = OpenItems.getInstance().getBlocks();
        Set<Block> candidates = new LinkedHashSet<>(movedBlocks);

        if (facing != BlockFace.SELF) {
            // Head destination + cells that pushed blocks would crush (DESTROY reaction)
            candidates.add(piston.getRelative(facing));
            for (var block : movedBlocks)
                candidates.add(block.getRelative(facing));
        }

        List<Block> toDestroy = new ArrayList<>();
        for (var block : candidates) {
            var placedBlock = blocks.getPlacedBlocks().get(block);
            if (placedBlock == null)
                continue;

            CustomBlockModel model = placedBlock.getModel();
            if (!model.isFragile())
                continue;

            if (!placedBlock.canBeReplaced())
                return false;

            toDestroy.add(block);
        }

        for (var block : toDestroy)
            // Clear the world block so piston head / moving_piston can occupy the cell.
            // Leaving chorus/tripwire causes physics to re-apply and eat the piston head.
            blocks.destroyBlock(block, false, true);

        return true;
    }

    private void handlePistonMove(List<Block> movedBlocks, BlockFace direction) {
        var blocks = OpenItems.getInstance().getBlocks();
        Map<Block, Block> fromTo = new LinkedHashMap<>();

        for (var block : movedBlocks) {
            var placedBlock = blocks.getPlacedBlocks().get(block);
            if (placedBlock == null)
                continue;

            CustomBlockModel model = placedBlock.getModel();
            if (model.isFragile())
                continue;

            fromTo.put(block, block.getRelative(direction));
        }

        if (fromTo.isEmpty())
            return;

        // Relocate registry immediately so physics/break handlers stay correct mid-animation
        blocks.moveBlocks(fromTo, false);

        // Sync displays/models after piston animation finishes
        Bukkit.getScheduler().scheduleSyncDelayedTask(OpenItems.getInstance(),
                () -> blocks.syncMovedBlocks(fromTo.values()), 3L);
    }

    @EventHandler
    public void blockFromTo(BlockFromToEvent e) {
        var block = e.getToBlock();

        var blocks = OpenItems.getInstance().getBlocks();

        var placedBlock = blocks.getPlacedBlocks().get(block);

        if (placedBlock == null)
            return;

        if (placedBlock.canBeDestroyedByLiquid()) {

            var event = new CustomBlockDestroyedByLiquidEvent(e.getBlock(), placedBlock, e);

            Bukkit.getPluginManager().callEvent(event);

            e.setCancelled(true);
            if (event.isCancelled())
                return;


            blocks.destroyBlock(block, placedBlock.dropOnDestroyByLiquid(), true);
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
                placedBlock.getModel().apply(b, false);
            } else {
                CustomChorusModel.setDefaultId(mf);
                b.setBlockData(mf, false);
            }

        }
    }


    public void checkTripwire(Block b) {
        if (b.getType() != Material.TRIPWIRE)
            return;
        var placedBlock = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(b);

        var data = b.getBlockData();
        if (placedBlock == null) {
            CustomTripwireModel.setDefaultId((Tripwire) data);
            b.setBlockData(data, false);
            return;
        }
        var blockType = placedBlock.getModel();
        if (blockType == null || !blockType.applyOnPhysics() || !(blockType instanceof CustomTripwireModel))
            return;

        blockType.apply(b, false);
    }

    public void checkChorus(Block b) {
        if (b.getType() != Material.CHORUS_PLANT)
            return;
        var placedBlock = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(b);

        var data = b.getBlockData();
        if (placedBlock == null) {
            CustomChorusModel.setDefaultId((MultipleFacing) data);
            b.setBlockData(data, false);
            return;
        }
        var blockType = placedBlock.getModel();
        if (blockType == null || !blockType.applyOnPhysics() || !(blockType instanceof CustomChorusModel))
            return;

        blockType.apply(b, false);
    }

    public void checkNote(Block b) {
        if (b.getType() != Material.NOTE_BLOCK)
            return;
        var placedBlock = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(b);

        var data = b.getBlockData();
        if (placedBlock == null) {
            CustomNoteblockModel.setDefaultId((NoteBlock) data);
            b.setBlockData(data, false);
            return;
        }
        var blockType = placedBlock.getModel();
        if (blockType == null || !blockType.applyOnPhysics())
            return;
        if (!(blockType instanceof CustomNoteblockModel))
            return;

        blockType.apply(b, false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void blockPhysics(BlockPhysicsEvent e) {
        var block = e.getBlock();
        var sblock = e.getSourceBlock();

        var blocks = OpenItems.getInstance().getBlocks();
        if ((block.getType() == Material.TRIPWIRE || sblock.getType() == Material.TRIPWIRE) && OpenItemsConfig.enableTripwires) {
            e.setCancelled(true);
            checkTripwire(block);
            checkTripwire(sblock);
            return;
        }

        if (OpenItemsConfig.enableChorus && block.getType() == Material.CHORUS_PLANT) {
            // Do not cancel / restore chorus while a piston is breaking or replacing it
            if (isPistonRelated(sblock.getType()) || isPistonRelated(e.getChangedType()))
                return;

            e.setCancelled(true);
            checkChorus(block);
            return;
        }

        if (block.getType() == Material.NOTE_BLOCK || sblock.getType() == Material.NOTE_BLOCK) {
            e.setCancelled(true);
            checkNote(block);
            checkNote(sblock);
            return;
        }

        //chorus check.
        if (OpenItemsConfig.enableChorus) {
            if (isPistonRelated(block.getType()) || isPistonRelated(sblock.getType()))
                return;

            boolean canCancel =
                    !OpenItemsConfig.allowedChorusUpdateBlocks.contains(block.getType()) &&
                            !OpenItemsConfig.allowedChorusUpdateBlocks.contains(sblock.getType());

            for (var face : Utils.getMainBlockFaces()) {
                var relative = block.getRelative(face);
                if (relative.getType() == Material.CHORUS_PLANT) {
                    if (blocks.getPlacedBlocks().containsKey(relative)) {
                        var placedBlock = blocks.getPlacedBlocks().get(relative);

                        if (placedBlock != null) {
                            if (placedBlock.canBeDestroyedByLiquid() && block.getType() == Material.WATER) {
                                var event = new CustomBlockDestroyedByLiquidEvent(relative, placedBlock, e);

                                Bukkit.getPluginManager().callEvent(event);

                                if (!event.isCancelled()) {
                                    suppressChorusFruit(relative);
                                    blocks.destroyBlock(relative, placedBlock.dropOnDestroyByLiquid(), true);
                                }
                            } else {
                                if (OpenItemsConfig.allowChorusPhysicsCancel && canCancel) {
                                    checkChorus(relative);
                                    e.setCancelled(true);
                                    return;
                                }
                                Bukkit.getScheduler().scheduleSyncDelayedTask(OpenItems.getInstance(), () -> {
                                    placedBlock.getModel().apply(relative, false);
                                }, 2);
                            }
                        }
                    }

                }
            }


            //fallback check
            var placedBlock = blocks.getPlacedBlocks().get(block);
            if (placedBlock != null) {
                var blockType = placedBlock.getModel();

                if (blockType != null && blockType.applyOnPhysics()) {
                    blockType.apply(block, false);
                }
            }
        }

    }

    private boolean isPistonRelated(Material type) {
        return type == Material.PISTON
                || type == Material.STICKY_PISTON
                || type == Material.PISTON_HEAD
                || type == Material.MOVING_PISTON;
    }

    private void suppressChorusFruit(Block block) {
        int now = Bukkit.getCurrentTick();
        suppressChorusFruitUntilTick.entrySet().removeIf(e -> e.getValue() < now);
        suppressChorusFruitUntilTick.put(block, now + 5);
    }

    private boolean consumeChorusFruitSuppression(Block block) {
        Integer until = suppressChorusFruitUntilTick.remove(block);
        return until != null && Bukkit.getCurrentTick() <= until;
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void dropChorus(ItemSpawnEvent e) {
        var item = e.getEntity();

        if (item.getItemStack().getType() != Material.CHORUS_FRUIT)
            return;

        var block = e.getLocation().getBlock();
        var blocks = OpenItems.getInstance().getBlocks();

        // Suppress vanilla fruit from our intentional destroys (e.g. liquid)
        if (consumeChorusFruitSuppression(block)) {
            e.setCancelled(true);
            return;
        }

        // Neighbor physics tried to break a still-registered custom chorus — cancel drop and restore
        var placedBlock = blocks.getPlacedBlocks().get(block);
        if (placedBlock != null && placedBlock.getModel() instanceof CustomChorusModel) {
            e.setCancelled(true);
            OpenItems.sync(() -> {
                if (blocks.getPlacedBlocks().containsKey(block))
                    placedBlock.getModel().apply(block, false);
            });
        }
    }

    /**
     * Prevent combining half slabs when one side is vanilla and the other is custom,
     * or when two different custom slab ids / drop materials would merge.
     * Same custom id + same {@code itemToDrop} material may form a double slab.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void preventInvalidSlabMerge(BlockPlaceEvent e) {
        var hand = e.getItemInHand();
        if (hand == null || hand.getType().isAir() || !Tag.SLABS.isTagged(hand.getType()))
            return;

        var halfBlock = findSlabMergeHalf(e, hand);
        if (halfBlock == null)
            return;

        var blocks = OpenItems.getInstance().getBlocks();
        var store = blocks.getPlacedBlocks().get(halfBlock);
        var handId = NBTUtil.getBlockId(hand);

        if (store == null) {
            // vanilla half + custom slab in hand
            if (handId != null)
                e.setCancelled(true);
            return;
        }

        // custom half + vanilla slab in hand
        if (handId == null) {
            e.setCancelled(true);
            return;
        }

        // different custom ids
        if (!handId.equals(store.getId())) {
            e.setCancelled(true);
            return;
        }

        // same id but different host / drop material
        var drop = store.getItemToDrop();
        if (drop == null || drop.getType() != hand.getType())
            e.setCancelled(true);
    }

    /**
     * @return the half-slab block being combined into a double, or null if this place is not a merge
     */
    private Block findSlabMergeHalf(BlockPlaceEvent e, ItemStack hand) {
        var against = e.getBlockAgainst();
        if (against != null
                && Tag.SLABS.isTagged(against.getType())
                && against.getType() == hand.getType()
                && against.getBlockData() instanceof Slab againstSlab
                && againstSlab.getType() != Slab.Type.DOUBLE) {
            if (e.getBlock().equals(against)
                    || e.getBlockReplacedState().getType() == against.getType())
                return against;
        }

        var replaced = e.getBlockReplacedState();
        if (Tag.SLABS.isTagged(replaced.getType())
                && replaced.getType() == hand.getType()
                && replaced.getBlockData() instanceof Slab replacedSlab
                && replacedSlab.getType() != Slab.Type.DOUBLE)
            return e.getBlock();

        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
        item = NBTUtil.clearPlacementId(item);
        var placedBlock = new BlockLocationStore(block.getX(), block.getY(), block.getZ(), item);


        var event = new CustomBlockPlaceEvent(block, placedBlock, e);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            e.setCancelled(true);
            return;
        }

        blocks.getPlacedBlocks().put(block, placedBlock);
        blockType.apply(block, true);

        blocks.saveChunk(block.getChunk());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void interact(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        var player = e.getPlayer();
        var block = e.getClickedBlock();
        var item = player.getInventory().getItemInMainHand();


        var blocks = OpenItems.getInstance().getBlocks();

        var placedBlock = blocks.getPlacedBlocks().get(block);

        if (placedBlock == null)
            return;

        double modifier = BlockHardnessUtil.resolveBreakSpeedModifier(block, item, placedBlock);

        CustomBlockSpeedModifierSetEvent event = new CustomBlockSpeedModifierSetEvent(block, placedBlock, player,
                item, modifier);

        Bukkit.getPluginManager().callEvent(event);

        modifier = event.getModifier();

        blocks.getBlockBreakSpeedModifiers().apply(player, modifier);
    }

    @EventHandler
    public void blockDamageAbort(BlockDamageAbortEvent e) {
        OpenItems.getInstance().getBlocks().getBlockBreakSpeedModifiers().remove(e.getPlayer());
    }

    @EventHandler
    public void blockBreakEvent(BlockBreakEvent e) {
        OpenItems.getInstance().getBlocks().getBlockBreakSpeedModifiers().remove(e.getPlayer());
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

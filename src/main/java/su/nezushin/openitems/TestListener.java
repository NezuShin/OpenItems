package su.nezushin.openitems;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.block.data.type.Tripwire;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.persistence.PersistentDataType;
import su.nezushin.openitems.blocks.CustomBlocks;
import su.nezushin.openitems.blocks.types.CustomNoteblockType;

public class TestListener implements Listener {

    @EventHandler
    public void place(BlockPlaceEvent e) {
        Block b = e.getBlock();
        if (e.getItemInHand() == null || e.getItemInHand().getType() != Material.STONE)
            return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(OpenItems.getInstance(), () -> {
            b.setType(Material.CHORUS_PLANT);
            CustomBlocks.setId(b, "123");
            if (b.getBlockData() instanceof MultipleFacing mf) {


                for (var i : Utils.getBlockFacesForChorus()) {
                    if (i == BlockFace.UP) {
                        mf.setFace(i, true);
                        continue;
                    }
                    mf.setFace(i, false);
                    //blocks.add(bl);
                }
                b.setBlockData(mf);
                b.getState().update(true, true);
                /*Bukkit.getScheduler().scheduleSyncDelayedTask(OpenItems.getInstance(), () -> {
                    for (var i : blocks)
                        this.blocks.remove(i);
                }, 3);*/
            }
        });
    }


    @EventHandler
    public void place2(BlockPlaceEvent e) {
        Block b = e.getBlock();
        if (e.getItemInHand() == null || e.getItemInHand().getType() != Material.COBBLESTONE)
            return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(OpenItems.getInstance(), () -> {

            b.getChunk().getPersistentDataContainer().set(new NamespacedKey(OpenItems.getInstance(), "123"), PersistentDataType.STRING, "");


            b.setType(Material.NOTE_BLOCK);
            CustomBlocks.setId(b, "123");
            if (b.getBlockData() instanceof NoteBlock nb) {

                /*nb.setInstrument(Instrument.BASS_DRUM);
                nb.setNote(new Note(1));
                b.setBlockData(nb);*/
                CustomNoteblockType.setId(nb, 0b11111);

                b.setBlockData(nb);
                b.getState().update(true, true);
                /*Bukkit.getScheduler().scheduleSyncDelayedTask(OpenItems.getInstance(), () -> {
                    for (var i : blocks)
                        this.blocks.remove(i);
                }, 3);*/
            }
        });
    }

    @EventHandler
    public void place3(BlockPlaceEvent e) {
        Block b = e.getBlock();
        if (e.getItemInHand() == null || e.getItemInHand().getType() != Material.SAND)
            return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(OpenItems.getInstance(), () -> {

            b.setType(Material.TRIPWIRE);
            CustomBlocks.setId(b, "123");
            if (b.getBlockData() instanceof Tripwire str) {

                /*nb.setInstrument(Instrument.BASS_DRUM);
                nb.setNote(new Note(1));
                b.setBlockData(nb);*/
                str.setPowered(true);


                b.setBlockData(str);
                b.getState().update(true, true);
                /*Bukkit.getScheduler().scheduleSyncDelayedTask(OpenItems.getInstance(), () -> {
                    for (var i : blocks)
                        this.blocks.remove(i);
                }, 3);*/
            }
        });
    }

    @EventHandler
    public void click(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        Block b = e.getClickedBlock();
        if (b == null)
            return;
        if (CustomBlocks.getId(b) != null && !e.getPlayer().isSneaking()) {
            e.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

    }




    @EventHandler
    public void breakBlock(BlockBreakEvent e) {
        e.getPlayer().sendMessage("123");
    }

    @EventHandler
    public void blockPhysics(BlockPhysicsEvent e) {
        var block = e.getBlock();
        /*if (!block.getType().equals(Material.CHORUS_PLANT))
            return;*/

        if (CustomBlocks.getId(block) != null) {
            e.setCancelled(true);
            return;
        }
        for (var face : Utils.getBlockFacesForChorus())
            if (CustomBlocks.getId(block.getRelative(face)) != null) {
                e.setCancelled(true);
                return;
            }
    }


    @EventHandler
    public void blockBreakBlockEvent(BlockBreakBlockEvent e) {
        if (CustomBlocks.getId(e.getBlock()) != null) {
            System.out.println(123333);

            return;
        }
    }

    @EventHandler
    public void entitiesUnloadEvent(EntitiesUnloadEvent e) {
        e.getEntities().forEach(i -> {
            if (i.getScoreboardTags().contains("CNBT_CUSTOM_BLOCK")) {

                i.remove();
            }
        });
    }
}

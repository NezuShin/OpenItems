package su.nezushin.openitems.blocks.types;

import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.block.data.type.Tripwire;
import su.nezushin.openitems.Utils;

import java.util.Arrays;

public class CustomStringType implements CustomBlockType {
    private int id;

    @Override
    public void apply(Block b) {
        b.setType(Material.TRIPWIRE);
        if (b.getBlockData() instanceof Tripwire t) {
            setId(t, this.id);
            b.setBlockData(t);
            b.getState().update(true, true);
        }
    }

    @Override
    public boolean isSimilar(Block b) {
        return b.getBlockData() instanceof Tripwire t && getId(t) == this.id;
    }

    public static void setId(Tripwire nb, int id) {
        id++;
        nb.setFace(BlockFace.SOUTH, ((id) & 0b1) == 1);
        nb.setFace(BlockFace.NORTH, ((id >> 1) & 0b1) == 1);
        nb.setFace(BlockFace.EAST, ((id >> 2) & 0b1) == 1);
        nb.setFace(BlockFace.WEST, ((id >> 3) & 0b1) == 1);

        nb.setPowered(((id >> 4) & 0b1) == 1);
        nb.setAttached(((id >> 5) & 0b1) == 1);

    }

    public static int getId(Tripwire nb) {
        var num = 0;

        num = (num << 1) | (nb.hasFace(BlockFace.SOUTH) ? 1 : 0);
        num = (num << 1) | (nb.hasFace(BlockFace.NORTH) ? 1 : 0);
        num = (num << 1) | (nb.hasFace(BlockFace.EAST) ? 1 : 0);
        num = (num << 1) | (nb.hasFace(BlockFace.WEST) ? 1 : 0);

        num = (num << 1) | (nb.isPowered() ? 1 : 0);
        num = (num << 1) | (nb.isAttached() ? 1 : 0);

        return num - 1;
    }
}

package su.nezushin.openitems.blocks.types;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Tripwire;
import su.nezushin.openitems.Utils;

public class CustomChorusType implements CustomBlockType {
    private int id;

    public CustomChorusType(int id) {
        this.id = id;
    }

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

    public static void setId(MultipleFacing nb, int id) {
        for (var i : Utils.getBlockFacesForChorus()) {
            nb.setFace(i, (id & 1) == 1);
            id = id >> 1;
        }
    }

    public static int getId(MultipleFacing nb) {
        var num = 0;
        for (var i : Utils.getBlockFacesForChorus()) {
            num = (num << 1) | (nb.hasFace(i) ? 1 : 0);
        }

        return num;
    }
}

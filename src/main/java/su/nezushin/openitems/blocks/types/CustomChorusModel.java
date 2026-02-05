package su.nezushin.openitems.blocks.types;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import su.nezushin.openitems.utils.Utils;

public class CustomChorusModel implements CustomBlockModel {
    private int id;

    public CustomChorusModel(int id) {
        this.id = id;
    }

    @Override
    public void apply(Block b, boolean update) {
        b.setType(Material.CHORUS_PLANT, update);
        if (b.getBlockData() instanceof MultipleFacing t) {
            setId(t, this.id);
            b.setBlockData(t, update);
            //b.getState().update(true, true);
        }
    }

    @Override
    public void apply(BlockData b) {
        if (!(b instanceof MultipleFacing t))
            return;

        setId(t, this.id);
    }

    @Override
    public boolean isSimilar(Block b) {
        return b.getBlockData() instanceof MultipleFacing t && getId(t) == this.id;
    }

    @Override
    public boolean applyOnPhysics() {
        return true;
    }

    public static void setId(MultipleFacing nb, int id) {
        id--;
        for (var i : Utils.getMainBlockFaces()) {
            nb.setFace(i, (id & 1) == 1);
            id = id >> 1;
        }
    }

    public static void setDefaultId(MultipleFacing multipleFacing) {
        setId(multipleFacing, 64);
    }

    public static int getId(MultipleFacing nb) {
        var num = 0;
        for (var i : Utils.getMainBlockFaces()) {
            num = (num << 1) | (nb.hasFace(i) ? 1 : 0);
        }

        return num + 1;
    }

    public static String toBlocksate(int id) {
        id--;
        return "down=" + (((id) & 0b1) == 1) +
                ",up=" + (((id >> 1) & 0b1) == 1) +
                ",south=" + (((id >> 2) & 0b1) == 1) +
                ",north=" + (((id >> 3) & 0b1) == 1) +
                ",east=" + (((id >> 4) & 0b1) == 1) +
                ",west=" + (((id >> 5) & 0b1) == 1);
    }
}

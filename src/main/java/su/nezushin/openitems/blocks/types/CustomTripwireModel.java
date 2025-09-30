package su.nezushin.openitems.blocks.types;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Tripwire;

public class CustomTripwireModel implements CustomBlockModel {
    private int id;

    public CustomTripwireModel(int id) {
        this.id = id;
    }

    @Override
    public void apply(Block b) {
        b.setType(Material.TRIPWIRE);
        if (b.getBlockData() instanceof Tripwire t) {
            setId(t, this.id);
            b.setBlockData(t);
            //b.getState().update(false, false);
        }
    }

    @Override
    public void apply(BlockData b) {
        if (!(b instanceof Tripwire t))
            return;

        setId(t, this.id);
    }

    @Override
    public boolean isSimilar(Block b) {
        return b.getBlockData() instanceof Tripwire t && getId(t) == this.id;
    }

    @Override
    public boolean applyOnPhysics() {
        return true;
    }

    public static void setId(Tripwire nb, int id) {
        id++;
        nb.setFace(BlockFace.SOUTH, ((id) & 0b1) == 1);
        nb.setFace(BlockFace.NORTH, ((id >> 1) & 0b1) == 1);
        nb.setFace(BlockFace.EAST, ((id >> 2) & 0b1) == 1);
        nb.setFace(BlockFace.WEST, ((id >> 3) & 0b1) == 1);

        nb.setPowered(((id >> 4) & 0b1) == 1);
        nb.setAttached(((id >> 5) & 0b1) == 1);
        nb.setDisarmed(((id >> 6) & 0b1) == 1);

    }

    public static void setDefaultId(Tripwire nb) {
        nb.setFace(BlockFace.SOUTH, false);
        nb.setFace(BlockFace.NORTH, false);
        nb.setFace(BlockFace.EAST, false);
        nb.setFace(BlockFace.WEST, false);

        nb.setPowered(false);
        nb.setAttached(false);
        nb.setDisarmed(false);
    }

    public static int getId(Tripwire nb) {
        var num = 0;

        num = (num << 1) | (nb.hasFace(BlockFace.SOUTH) ? 1 : 0);
        num = (num << 1) | (nb.hasFace(BlockFace.NORTH) ? 1 : 0);
        num = (num << 1) | (nb.hasFace(BlockFace.EAST) ? 1 : 0);
        num = (num << 1) | (nb.hasFace(BlockFace.WEST) ? 1 : 0);

        num = (num << 1) | (nb.isPowered() ? 1 : 0);
        num = (num << 1) | (nb.isAttached() ? 1 : 0);
        num = (num << 1) | (nb.isDisarmed() ? 1 : 0);

        return num - 1;
    }

    public static String toBlocksate(int id) {
        id++;
        return "south=" + (((id) & 0b1) == 1) +
                ",north=" + (((id >> 1) & 0b1) == 1) +
                ",east=" + (((id >> 2) & 0b1) == 1) +
                ",west=" + (((id >> 3) & 0b1) == 1) +
                ",powered=" + (((id >> 4) & 0b1) == 1) +
                ",attached=" + (((id >> 5) & 0b1) == 1) +
                ",disarmed=" + (((id >> 6) & 0b1) == 1);
    }
}

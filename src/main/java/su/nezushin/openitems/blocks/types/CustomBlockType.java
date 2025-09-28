package su.nezushin.openitems.blocks.types;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public abstract class CustomBlockType {

    private boolean canBurn = true, canBeBlown = true, canBeReplaced = true, canBeBrokenByLiquid = true;


    public abstract void apply(Block b);

    public abstract void apply(BlockData b);

    public abstract boolean isSimilar(Block b);

    public abstract boolean applyOnPhysics();

    public boolean canBeBlown() {
        return canBeBlown;
    }

    public boolean canBurn() {
        return canBurn;
    }

    public boolean canBeReplaced() {
        return canBeReplaced;
    }

    public boolean canBeBrokenByLiquid() {
        return canBeBrokenByLiquid;
    }
}

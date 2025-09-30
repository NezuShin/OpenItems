package su.nezushin.openitems.blocks.types;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public interface CustomBlockModel {

    public void apply(Block b);

    public void apply(BlockData b);

    public boolean isSimilar(Block b);

    public boolean applyOnPhysics();
}

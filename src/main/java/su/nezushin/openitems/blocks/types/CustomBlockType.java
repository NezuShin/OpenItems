package su.nezushin.openitems.blocks.types;

import org.bukkit.block.Block;

public interface CustomBlockType {

    public void apply(Block b);

    public boolean isSimilar(Block b);

}

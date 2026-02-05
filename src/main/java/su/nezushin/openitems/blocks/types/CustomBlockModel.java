package su.nezushin.openitems.blocks.types;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;


public interface CustomBlockModel {


    /**
     * Place custom block
     *
     * @param b block to place
     * @papam update false to cancel physics from the changed block
     */
    public void apply(Block b, boolean update);

    /**
     * Set right block data to block (used with applyOnPhysics=true; Needed for tripwire proper work)
     *
     * @param b - block data to change
     */
    public void apply(BlockData b);

    /**
     * Check if block has this model
     *
     * @param b - block to check
     * @return is block has this model
     */
    public boolean isSimilar(Block b);


    /**
     * @return should listener apply(BlockData) on BlockPhysicsEvent or not
     */
    public boolean applyOnPhysics();
}

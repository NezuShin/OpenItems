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

    /**
     * Clean up model-specific entities/state when the custom block is removed or unloaded.
     */
    default void remove(Block b) {
    }
}

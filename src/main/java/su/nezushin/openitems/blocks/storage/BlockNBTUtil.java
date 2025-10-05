package su.nezushin.openitems.blocks.storage;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

public class BlockNBTUtil {

    /**
     * Get block model from item
     *
     * @return model id
     */
    public static String getBlockId(ItemStack item) {
        var nbtItem = new NBTItem(item);

        var customBlockData = nbtItem.getCompound("openitems_custom_block");

        if (customBlockData == null)
            return null;

        return customBlockData.getString("id");
    }


    /**
     * Set block model to item
     *
     * @param id - model id
     * @return new item with set block model
     */
    public static ItemStack setBlockId(ItemStack item, String id) {
        var nbtItem = new NBTItem(item);
        var customBlockData = nbtItem.getOrCreateCompound("openitems_custom_block");

        customBlockData.setString("id", id);

        return nbtItem.getItem();
    }


    /**
     * Get data to be applied when block placed
     *
     * @return block data to be applied
     */
    public static BlockDataStore getBlockData(ItemStack item) {
        var blockStore = new BlockDataStore(item);

        if (!blockStore.load())
            return null;
        return blockStore;
    }

    /**
     * Clear any info about block behavior
     *
     * @param item item to clear
     * @return new item without block data
     */
    public static ItemStack clearBlockId(ItemStack item) {
        var nbtItem = new NBTItem(item);

        nbtItem.removeKey("openitems_custom_block");

        return nbtItem.getItem();
    }
}

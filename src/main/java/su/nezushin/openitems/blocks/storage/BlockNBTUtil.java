package su.nezushin.openitems.blocks.storage;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

public class BlockNBTUtil {

    public static String getBlockId(ItemStack item) {
        var nbtItem = new NBTItem(item);

        var customBlockData = nbtItem.getCompound("openitems_custom_block");

        if (customBlockData == null)
            return null;

        return customBlockData.getString("id");
    }

    public static ItemStack setBlockId(ItemStack item, String id) {
        var nbtItem = new NBTItem(item);
        var customBlockData = nbtItem.getOrCreateCompound("openitems_custom_block");

        customBlockData.setString("id", id);

        return nbtItem.getItem();
    }

    public static BlockDataStore getBlockData(ItemStack item) {
        var blockStore = new BlockDataStore(item);

        if (!blockStore.load())
            return null;
        return blockStore;
    }

    public static ItemStack clearBlockId(ItemStack item) {
        var nbtItem = new NBTItem(item);

        nbtItem.removeKey("openitems_custom_block");

        return nbtItem.getItem();
    }
}

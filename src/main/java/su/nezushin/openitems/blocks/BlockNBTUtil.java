package su.nezushin.openitems.blocks;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

public class BlockNBTUtil {

    public static String getBlockId(ItemStack item) {
        return new NBTItem(item).getString("openitems_custom_block_id");
    }

    public static ItemStack setBlockId(ItemStack item, String id) {
        var nbtItem = new NBTItem(item);

        nbtItem.setString("openitems_custom_block_id", id);

        return nbtItem.getItem();
    }

    public static ItemStack clearBlockId(ItemStack item) {
        var nbtItem = new NBTItem(item);

        nbtItem.removeKey("openitems_custom_block_id");

        return nbtItem.getItem();
    }
}

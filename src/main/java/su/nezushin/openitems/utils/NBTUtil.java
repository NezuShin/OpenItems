package su.nezushin.openitems.utils;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.blocks.storage.BlockDataStore;

import java.util.List;

public class NBTUtil {

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


    /**
     * Not used now.
     * Need this functionality to prevent helmet durability damage on falling
     *
     * @param item
     * @param damageTypes damage types from registry to prevent armor durability damage
     * @return item with set nbt tag openitems_ignore_damage_types
     */
    public static ItemStack setIgnoreDamageCauses(ItemStack item, List<String> damageTypes) {
        var nbtItem = new NBTItem(item);
        var stringList = nbtItem.getStringList("openitems_ignore_damage_causes");

        stringList.clear();
        stringList.addAll(damageTypes);

        return nbtItem.getItem();
    }

    /**
     * Not used now.
     * Need this functionality to prevent helmet durability damage on falling
     *
     * @param item
     * @return nbt tag openitems_ignore_damage_types
     */
    public static List<String> getIgnoreDamageCauses(ItemStack item) {
        var nbtItem = new NBTItem(item);
        var stringList = nbtItem.getStringList("openitems_ignore_damage_causes");
        return stringList.toListCopy();
    }

    /**
     * Not used now.
     * Need this functionality to prevent helmet durability damage on falling
     *
     * @param item
     * @param damageType damage type from registry
     * @return has it damageType in openitems_ignore_damage_types array
     */
    public static boolean hasIgnoreDamageCause(ItemStack item, String damageType) {
        var nbtItem = new NBTItem(item);
        return nbtItem.getStringList("openitems_ignore_damage_causes").contains(damageType);
    }
}

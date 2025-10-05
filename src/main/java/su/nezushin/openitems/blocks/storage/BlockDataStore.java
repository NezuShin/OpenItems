package su.nezushin.openitems.blocks.storage;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.types.CustomBlockModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents custom block properties. Used to be stored in custom item data
 */
public class BlockDataStore {
    protected boolean canBurn = true, canBeBlown = true, canBeReplaced = true, dropOnDestroy = true;

    protected String id;

    protected ItemStack itemToDrop;

    public BlockDataStore(ItemStack itemToDrop) {
        this.itemToDrop = itemToDrop;
        this.load();
    }

    public BlockDataStore() {
    }

    public boolean load() {

        if (this.itemToDrop == null)
            return false;

        NBTItem nbtItem = new NBTItem(this.itemToDrop);
        var compound = nbtItem.getCompound("openitems_custom_block");
        if (compound == null)
            return false;

        id = compound.getString("id");
        canBeBlown = compound.getBoolean("can_be_blown");
        canBeReplaced = compound.getBoolean("can_be_replaced");
        canBurn = compound.getBoolean("can_burn");
        dropOnDestroy = compound.getBoolean("drop_on_destroy");

        return true;
    }

    public ItemStack applyData() {
        NBTItem nbtItem = new NBTItem(this.itemToDrop);
        var compound = nbtItem.getOrCreateCompound("openitems_custom_block");

        compound.setString("id", this.id);
        compound.setBoolean("can_be_blown", this.canBeBlown);
        compound.setBoolean("can_be_replaced", this.canBeReplaced);
        compound.setBoolean("can_burn", this.canBurn);
        compound.setBoolean("drop_on_destroy", this.dropOnDestroy);


        return this.itemToDrop = nbtItem.getItem();
    }


    public boolean canBeBlown() {
        return canBeBlown;
    }

    public boolean canBurn() {
        return canBurn;
    }

    public boolean canBeReplaced() {
        return canBeReplaced;
    }

    public boolean dropOnDestroy() {
        return dropOnDestroy;
    }

    public void setCanBurn(boolean canBurn) {
        this.canBurn = canBurn;
    }

    public void setCanBeBlown(boolean canBeBlown) {
        this.canBeBlown = canBeBlown;
    }

    public void setCanBeReplaced(boolean canBeReplaced) {
        this.canBeReplaced = canBeReplaced;
    }

    /**
     * Set should itemToDrop be dropped
     */
    public void setDropOnDestroy(boolean dropOnDestroy) {
        this.dropOnDestroy = dropOnDestroy;
    }

    /**
     * @return custom block model id
     */
    public String getId() {
        return id;
    }

    /**
     * @return custom block model
     */
    public CustomBlockModel getModel() {
        return OpenItems.getInstance().getModelRegistry().getBlockTypes().get(this.id);
    }


    /**
     * @return item used to place this custom block
     */
    public ItemStack getItemToDrop() {
        return itemToDrop;
    }
}

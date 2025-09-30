package su.nezushin.openitems.blocks;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

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

    public void setDropOnDestroy(boolean dropOnDestroy) {
        this.dropOnDestroy = dropOnDestroy;
    }

    public String getId() {
        return id;
    }

    public ItemStack getItemToDrop() {
        return itemToDrop;
    }
}

package su.nezushin.openitems.blocks.storage;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.types.CustomBlockModel;

/**
 * Represents custom block properties. Used to be stored in custom item data
 */
public class BlockDataStore {
    protected boolean canBurn = true, canBeBlown = true, canBeReplaced = true, dropOnBreak = true,
            dropOnExplosion = true, dropOnDestroyByLiquid = true, canBeDestroyedByLiquid = true, dropOnBurn = true;

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
        dropOnBreak = compound.getBoolean("drop_on_break");
        dropOnExplosion = compound.getBoolean("drop_on_explosion");
        dropOnDestroyByLiquid = compound.getBoolean("drop_on_destroy_by_liquid");

        return true;
    }

    public ItemStack applyData() {
        NBTItem nbtItem = new NBTItem(this.itemToDrop);
        var compound = nbtItem.getOrCreateCompound("openitems_custom_block");

        compound.setString("id", this.id);
        compound.setBoolean("can_be_blown", this.canBeBlown);
        compound.setBoolean("can_be_replaced", this.canBeReplaced);
        compound.setBoolean("can_burn", this.canBurn);
        compound.setBoolean("drop_on_break", this.dropOnBreak);
        compound.setBoolean("drop_on_explosion", this.dropOnExplosion);
        compound.setBoolean("drop_on_destroy_by_liquid", this.dropOnDestroyByLiquid);


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

    public boolean dropOnBreak() {
        return dropOnBreak;
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

    public void setDropOnExplosion(boolean dropOnExplosion) {
        this.dropOnExplosion = dropOnExplosion;
    }

    public void setDropOnDestroyByLiquid(boolean dropOnDestroyByLiquid) {
        this.dropOnDestroyByLiquid = dropOnDestroyByLiquid;
    }

    public boolean canBeDestroyedByLiquid() {
        return canBeDestroyedByLiquid;
    }

    public void setCanBeDestroyedByLiquid(boolean canBeDestroyedByLiquid) {
        this.canBeDestroyedByLiquid = canBeDestroyedByLiquid;
    }

    public boolean dropOnDestroyByLiquid() {
        return dropOnDestroyByLiquid;
    }

    public boolean dropOnExplosion() {
        return dropOnExplosion;
    }

    public boolean dropOnBurn() {
        return dropOnBurn;
    }

    public void setDropOnBurn(boolean dropOnBurn) {
        this.dropOnBurn = dropOnBurn;
    }

    /**
     * Set should itemToDrop be dropped when player break block
     */
    public void setDropOnBreak(boolean dropOnBreak) {
        this.dropOnBreak = dropOnBreak;
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

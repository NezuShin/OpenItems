package su.nezushin.openitems.blocks;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

public class BlockLocationStore extends BlockDataStore {
    private int x, y, z;


    public BlockLocationStore(int x, int y, int z, ItemStack itemToDrop) {
        super(itemToDrop);
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public BlockLocationStore() {
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public ItemStack getItemToDrop() {
        return itemToDrop;
    }
}

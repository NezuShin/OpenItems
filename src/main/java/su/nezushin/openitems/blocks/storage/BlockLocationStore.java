package su.nezushin.openitems.blocks.storage;

import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents placed custom block properties. Used to be stored in custom chunk data
 */
public class BlockLocationStore extends BlockDataStore {
    private int x, y, z;

    protected Map<String, Object> arbitraryData = new HashMap<>();

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


    /**
     * Any arbitrary data of custom block can be stored here.
     * Note that after setting arbitrary data you need to save chunk manually using {@code OpenItems.getInstance().getBlocks().saveChunk(chunk);}
     *
     * @return Map with arbitrary data. Can store everything implements ConfigurationSerializable
     */
    public Map<String, Object> getArbitraryData() {
        return arbitraryData;
    }
}

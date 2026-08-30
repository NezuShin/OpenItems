package su.nezushin.openitems.blocks.storage;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents placed custom block properties. Used to be stored in custom chunk data
 */
public class BlockLocationStore extends BlockDataStore {
    private int x, y, z;

    private final Map<String, Object> extras = new HashMap<>();

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

    public void setLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Plugin-owned per-block data not stored in item NBT.
     * After changing extras, save the chunk via {@code OpenItems.getInstance().getBlocks().saveChunk(chunk)}.
     */
    public Map<String, Object> getExtras() {
        return extras;
    }

    public void putExtra(String key, Object value) {
        extras.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtra(String key, Class<T> type) {
        Object value = extras.get(key);
        if (value == null)
            return null;
        return (T) value;
    }

    public void removeExtra(String key) {
        extras.remove(key);
    }

    @Override
    public String toString() {
        return "BlockLocationStore{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", extras=" + extras +
                ", canBurn=" + canBurn +
                ", canBeBlown=" + canBeBlown +
                ", canBeReplaced=" + canBeReplaced +
                ", dropOnBreak=" + dropOnBreak +
                ", dropOnExplosion=" + dropOnExplosion +
                ", dropOnDestroyByLiquid=" + dropOnDestroyByLiquid +
                ", canBeDestroyedByLiquid=" + canBeDestroyedByLiquid +
                ", dropOnBurn=" + dropOnBurn +
                ", id='" + id + '\'' +
                ", hardness=" + hardness +
                ", preferredTools=" + preferredTools +
                ", toolSpeedMultipliers=" + toolSpeedMultipliers +
                ", materialSpeedMultipliers=" + materialSpeedMultipliers +
                ", modelSpeedMultipliers=" + modelSpeedMultipliers +
                ", toolSpeedHasGradeMultiplier=" + toolSpeedHasGradeMultiplier +
                ", dropWhenMinedByTools=" + dropWhenMinedByTools +
                ", itemToDrop=" + itemToDrop +
                '}';
    }
}

package su.nezushin.openitems.blocks.storage;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents placed custom block properties. Used to be stored in custom chunk data
 */
public class BlockLocationStore extends BlockDataStore {
    private int x, y, z;

    protected Map<String, Object> arbitraryData = new HashMap<>();

    protected Map<String, ConfigurationSerializable> arbitraryBukkitData = new HashMap<>();

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
     * Any arbitrary data of custom block can be stored here. If you need to save ConfigurationSerializable use {@code getArbitraryBukkitData()} instead.
     * Note that after setting arbitrary data you need to save chunk manually using {@code OpenItems.getInstance().getBlocks().saveChunk(chunk);}
     *
     * @return Map with arbitrary data.
     */
    public Map<String, Object> getArbitraryData() {
        return arbitraryData;
    }

    /**
     * Any arbitrary instance of ConfigurationSerializable can be stored here. E.g. ItemStacks, Locations
     * Note that after setting arbitrary data you need to save chunk manually using {@code OpenItems.getInstance().getBlocks().saveChunk(chunk);}
     *
     * @return Map with arbitrary data. Can store everything implements ConfigurationSerializable
     */
    public Map<String, ConfigurationSerializable> getArbitraryBukkitData() {
        return arbitraryBukkitData;
    }

    @Override
    public String toString() {
        return "BlockLocationStore{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", arbitraryData=" + arbitraryData +
                ", arbitraryBukkitData=" + arbitraryBukkitData +
                ", canBurn=" + canBurn +
                ", canBeBlown=" + canBeBlown +
                ", canBeReplaced=" + canBeReplaced +
                ", dropOnBreak=" + dropOnBreak +
                ", dropOnExplosion=" + dropOnExplosion +
                ", dropOnDestroyByLiquid=" + dropOnDestroyByLiquid +
                ", canBeDestroyedByLiquid=" + canBeDestroyedByLiquid +
                ", dropOnBurn=" + dropOnBurn +
                ", id='" + id + '\'' +
                ", toolSpeedMultipliers=" + toolSpeedMultipliers +
                ", materialSpeedMultipliers=" + materialSpeedMultipliers +
                ", modelSpeedMultipliers=" + modelSpeedMultipliers +
                ", toolSpeedHasGradeMultiplier=" + toolSpeedHasGradeMultiplier +
                ", dropWhenMinedByTools=" + dropWhenMinedByTools +
                ", itemToDrop=" + itemToDrop +
                '}';
    }
}

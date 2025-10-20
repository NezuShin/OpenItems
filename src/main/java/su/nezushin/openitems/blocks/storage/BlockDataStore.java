package su.nezushin.openitems.blocks.storage;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.ToolItemType;
import su.nezushin.openitems.blocks.types.CustomBlockModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents custom block properties. Used to be stored in custom item data
 */
public class BlockDataStore {
    protected boolean canBurn = true, canBeBlown = true, canBeReplaced = true, dropOnBreak = true,
            dropOnExplosion = true, dropOnDestroyByLiquid = true, canBeDestroyedByLiquid = true, dropOnBurn = true;

    protected String id;

    protected Map<ToolItemType, Double> toolSpeedMultipliers = new HashMap<>();
    protected Map<Material, Double> materialSpeedMultipliers = new HashMap<>();
    protected Map<String, Double> modelSpeedMultipliers = new HashMap<>();

    protected Set<ToolItemType> toolSpeedHasGradeMultiplier = new HashSet<>();

    protected Set<ToolItemType> dropWhenMinedByTools = new HashSet<>();

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
        canBeDestroyedByLiquid = compound.getBoolean("can_be_destroyed_by_liquid");
        canBeReplaced = compound.getBoolean("can_be_replaced");
        canBurn = compound.getBoolean("can_burn");
        dropOnBreak = compound.getBoolean("drop_on_break");
        dropOnExplosion = compound.getBoolean("drop_on_explosion");
        dropOnDestroyByLiquid = compound.getBoolean("drop_on_destroy_by_liquid");

        dropWhenMinedByTools = new HashSet<>(compound.getStringList("drop_when_mined_by_tools")
                .stream().map(i -> ToolItemType.valueOf(i.toUpperCase())).toList());


        var speedMultiplier = compound.getCompound("speed_multiplier");

        if (speedMultiplier != null) {
            compoundToMap(speedMultiplier.getCompound("tools")).forEach((k, v) ->
                    toolSpeedMultipliers.put(ToolItemType.valueOf(k.toUpperCase()), v));
            compoundToMap(speedMultiplier.getCompound("materials")).forEach((k, v) ->
                    materialSpeedMultipliers.put(Material.valueOf(k.toUpperCase()), v));
            modelSpeedMultipliers.putAll(compoundToMap(speedMultiplier.getCompound("models")));

            var toolsList = speedMultiplier.getStringList("tools_has_grade_multiplier");
            toolSpeedHasGradeMultiplier = new HashSet<>(toolsList.stream().map(i -> ToolItemType.valueOf(i.toUpperCase())).toList());
        }

        return true;
    }

    private Map<String, Double> compoundToMap(NBTCompound compound) {
        Map<String, Double> map = new HashMap<>();

        if (compound != null) {
            for (var i : compound.getKeys()) {
                map.put(i, compound.getDouble(i));
            }
        }

        return map;
    }


    public ItemStack applyData() {
        NBTItem nbtItem = new NBTItem(this.itemToDrop);
        var compound = nbtItem.getOrCreateCompound("openitems_custom_block");

        compound.setString("id", this.id);
        compound.setBoolean("can_be_blown", this.canBeBlown);
        compound.setBoolean("can_be_replaced", this.canBeReplaced);
        compound.setBoolean("can_be_destroyed_by_liquid", this.canBeDestroyedByLiquid);
        compound.setBoolean("can_burn", this.canBurn);
        compound.setBoolean("drop_on_break", this.dropOnBreak);
        compound.setBoolean("drop_on_explosion", this.dropOnExplosion);
        compound.setBoolean("drop_on_destroy_by_liquid", this.dropOnDestroyByLiquid);

        var speedMultiplier = compound.getOrCreateCompound("speed_multiplier");

        var tools = speedMultiplier.getOrCreateCompound("tools");
        toolSpeedMultipliers.forEach((k, v) -> {
            if (v != -1)
                tools.setDouble(k.name(), v);
            else tools.removeKey(k.name());
        });

        var materials = speedMultiplier.getOrCreateCompound("materials");
        materialSpeedMultipliers.forEach((k, v) -> {
            if (v != -1)
                materials.setDouble(k.name(), v);
            else materials.removeKey(k.name());

        });

        var models = speedMultiplier.getOrCreateCompound("models");
        modelSpeedMultipliers.forEach((k, v) -> {
            if (v != -1)
                models.setDouble(k, v);
            else models.removeKey(k);
        });
        var list = speedMultiplier.getStringList("tools_has_grade_multiplier");
        list.addAll(toolSpeedHasGradeMultiplier.stream().map(Enum::name).toList());

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

    public Map<ToolItemType, Double> getToolSpeedMultipliers() {
        return toolSpeedMultipliers;
    }

    public Map<Material, Double> getMaterialSpeedMultipliers() {
        return materialSpeedMultipliers;
    }

    public Map<String, Double> getModelSpeedMultipliers() {
        return modelSpeedMultipliers;
    }

    public Set<ToolItemType> getToolSpeedHasGradeMultiplier() {
        return toolSpeedHasGradeMultiplier;
    }

    public Set<ToolItemType> dropWhenMinedByTools() {
        return dropWhenMinedByTools;
    }
}

package su.nezushin.openitems.blocks.storage;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.ToolItemType;
import su.nezushin.openitems.blocks.types.CustomBlockModel;
import su.nezushin.openitems.utils.NBTUtil;

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

    /**
     * Current runtime model id when it differs from {@link #id} (e.g. promoted slab double).
     */
    protected String overrideId;

    /**
     * Custom felt hardness. {@code null} means unset — use legacy tool speed maps.
     */
    protected Double hardness = null;

    protected Set<ToolItemType> preferredTools = new HashSet<>();

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
        overrideId = compound.hasTag("override_id") ? compound.getString("override_id") : null;
        canBeBlown = compound.getBoolean("can_be_blown");
        canBeDestroyedByLiquid = compound.getBoolean("can_be_destroyed_by_liquid");
        canBeReplaced = compound.getBoolean("can_be_replaced");
        canBurn = compound.getBoolean("can_burn");
        dropOnBreak = compound.getBoolean("drop_on_break");
        dropOnExplosion = compound.getBoolean("drop_on_explosion");
        dropOnDestroyByLiquid = compound.getBoolean("drop_on_destroy_by_liquid");

        dropWhenMinedByTools = new HashSet<>(compound.getStringList("drop_when_mined_by_tools")
                .stream().map(i -> ToolItemType.valueOf(i.toUpperCase())).toList());

        if (compound.hasTag("hardness"))
            hardness = compound.getDouble("hardness");
        else
            hardness = null;

        preferredTools = new HashSet<>(compound.getStringList("preferred_tools")
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
        if (this.overrideId != null)
            compound.setString("override_id", this.overrideId);
        else
            compound.removeKey("override_id");
        compound.setBoolean("can_be_blown", this.canBeBlown);
        compound.setBoolean("can_be_replaced", this.canBeReplaced);
        compound.setBoolean("can_be_destroyed_by_liquid", this.canBeDestroyedByLiquid);
        compound.setBoolean("can_burn", this.canBurn);
        compound.setBoolean("drop_on_break", this.dropOnBreak);
        compound.setBoolean("drop_on_explosion", this.dropOnExplosion);
        compound.setBoolean("drop_on_destroy_by_liquid", this.dropOnDestroyByLiquid);

        if (this.hardness != null)
            compound.setDouble("hardness", this.hardness);
        else
            compound.removeKey("hardness");

        var preferredList = compound.getStringList("preferred_tools");
        preferredList.clear();
        preferredList.addAll(preferredTools.stream().map(Enum::name).toList());

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
        list.clear();
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

    public String getOverrideId() {
        return overrideId;
    }

    public void setOverrideId(String overrideId) {
        this.overrideId = overrideId;
    }

    /**
     * Current runtime model id: {@code override_id} when set, otherwise real {@code id}.
     */
    public String getEffectiveBlockId() {
        return overrideId != null ? overrideId : id;
    }

    /**
     * Set block model id and save it. Note that already placed and registered blocks need
     * to be changed via OpenItems.getInstance().getBlocks().changeBlockModel(block, modelId);
     * @param id block model id
     */
    public void setId(String id) {
        this.id = id;
        this.applyData();
    }


    /**
     * @return custom block model ignoring override id
     */
    public CustomBlockModel getModel() {
        return OpenItems.getInstance().getModelRegistry().getBlockTypes().get(getEffectiveBlockId());
    }

    /**
     * @return custom block model (based on override_id when set; otherwise id)
     */
    public CustomBlockModel getCurrentModel() {
        return OpenItems.getInstance().getModelRegistry().getBlockTypes().get(getEffectiveBlockId());
    }

    /**
     * Clone of internal item with full stored state (includes {@code override_id} when set).
     */
    public ItemStack getCurrentItem() {
        return applyData();
    }

    /**
     * Clone for player drops: real {@code id}, never includes {@code placement_id}.
     */
    public ItemStack getItemToDrop() {
        return NBTUtil.clearOverrideId(itemToDrop.clone());
    }

    public boolean hasHardness() {
        return hardness != null;
    }

    public Double getHardness() {
        return hardness;
    }

    /**
     * Set custom hardness. Passing {@code null} clears it (legacy speed maps apply again).
     * Setting a value clears legacy per-tool maps so both styles do not pile up.
     */
    public void setHardness(Double hardness) {
        this.hardness = hardness;
        if (hardness != null) {
            this.toolSpeedMultipliers.clear();
            this.toolSpeedHasGradeMultiplier.clear();
        }
    }

    public Set<ToolItemType> getPreferredTools() {
        return preferredTools;
    }

    public void setPreferredTools(Set<ToolItemType> preferredTools) {
        this.preferredTools = preferredTools;
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

    public void setToolSpeedHasGradeMultiplier(Set<ToolItemType> toolSpeedHasGradeMultiplier) {
        this.toolSpeedHasGradeMultiplier = toolSpeedHasGradeMultiplier;
    }

    public Set<ToolItemType> dropWhenMinedByTools() {
        return dropWhenMinedByTools;
    }

    @Override
    public String toString() {
        return "BlockDataStore{" +
                "canBurn=" + canBurn +
                ", canBeBlown=" + canBeBlown +
                ", canBeReplaced=" + canBeReplaced +
                ", dropOnBreak=" + dropOnBreak +
                ", dropOnExplosion=" + dropOnExplosion +
                ", dropOnDestroyByLiquid=" + dropOnDestroyByLiquid +
                ", canBeDestroyedByLiquid=" + canBeDestroyedByLiquid +
                ", dropOnBurn=" + dropOnBurn +
                ", id='" + id + '\'' +
                ", overrideId='" + overrideId + '\'' +
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

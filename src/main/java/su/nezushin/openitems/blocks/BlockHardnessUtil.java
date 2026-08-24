package su.nezushin.openitems.blocks;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.blocks.storage.BlockDataStore;

/**
 * Helpers for custom-block break speed on top of a vanilla host block
 * (note block / chorus plant). See {@code docs/block-break-speed.md}.
 * <p>
 * Java break formula (simplified):
 * {@code damage = toolSpeed * block_break_speed * … / hostHardness / 30}.
 * OpenItems only sets {@code block_break_speed}; this class computes that value
 * so felt speed matches hardness + preferred tools (or legacy multipliers).
 */
public final class BlockHardnessUtil {

    private BlockHardnessUtil() {
    }

    /**
     * Resolve {@code block_break_speed} for a placed custom block.
     * <ol>
     *   <li>Matching {@code material} / {@code model} override (flat)</li>
     *   <li>If hardness is set → hardness + preferred tools</li>
     *   <li>Else legacy per-tool maps</li>
     * </ol>
     */
    public static double resolveBreakSpeedModifier(Block host, ItemStack tool, BlockDataStore data) {
        if (tool != null && !tool.getType().isAir()) {
            if (data.getMaterialSpeedMultipliers().containsKey(tool.getType()))
                return data.getMaterialSpeedMultipliers().get(tool.getType());

            var model = tool.getDataOrDefault(DataComponentTypes.ITEM_MODEL, null);
            if (model != null && data.getModelSpeedMultipliers().containsKey(model.asString()))
                return data.getModelSpeedMultipliers().get(model.asString());
        }

        if (data.hasHardness()) {
            ToolItemType heldType = (tool == null || tool.getType().isAir())
                    ? ToolItemType.HAND
                    : ToolItemType.valueOf(tool.getType());
            if (heldType == null)
                heldType = ToolItemType.HAND;

            boolean preferred = data.getPreferredTools().contains(heldType);
            return attributeForCustomHardness(host, tool, data.getHardness(), preferred);
        }

        return legacyToolMultiplier(tool, data);
    }

    private static double legacyToolMultiplier(ItemStack tool, BlockDataStore data) {
        if (tool == null || tool.getType().isAir())
            return data.getToolSpeedMultipliers().getOrDefault(ToolItemType.HAND, 1.0);

        var toolType = ToolItemType.valueOf(tool.getType());
        double modifier = data.getToolSpeedMultipliers().getOrDefault(toolType, 1.0);
        if (toolType != null && data.getToolSpeedHasGradeMultiplier().contains(toolType))
            modifier *= ToolItemType.getTypeModifier(tool.getType());
        return modifier;
    }

    /**
     * Vanilla hardness of the host block material.
     */
    public static float getHostHardness(Block block) {
        return block.getType().getHardness();
    }

    /**
     * Vanilla hardness of a material (e.g. {@link Material#STONE} → 1.5).
     */
    public static float getHardness(Material material) {
        return material.getHardness();
    }

    /**
     * Mining speed of {@code tool} against {@code block} before
     * {@code block_break_speed}, haste, water, etc.
     * Includes Efficiency when the tool is effective on this block ({@code speed > 1}).
     */
    public static float getVanillaToolSpeed(Block block, ItemStack tool) {
        if (tool == null || tool.getType().isAir()) {
            return 1.0f;
        }
        float speed = block.getDestroySpeed(tool, true);
        return speed <= 0 ? 1.0f : speed;
    }

    /**
     * {@code block_break_speed} so the held tool mines as fast as the given
     * desired tool-speed multiplier (wiki {@code speedMultiplier} before haste/water).
     * <p>
     * {@code attribute = desiredToolSpeed / vanillaToolSpeed}
     */
    public static double attributeForDesiredToolSpeed(Block host, ItemStack tool, double desiredToolSpeed) {
        if (desiredToolSpeed < 0) {
            desiredToolSpeed = 0;
        }
        float vanilla = getVanillaToolSpeed(host, tool);
        return desiredToolSpeed / vanilla;
    }

    /**
     * Every tool breaks as slowly as an empty hand on this host block.
     * Cancels axe preference on note blocks/chorus and Efficiency on those tools.
     */
    public static double attributeForHandSpeed(Block host, ItemStack tool) {
        return attributeForDesiredToolSpeed(host, tool, 1.0);
    }

    /**
     * {@code block_break_speed} so mining feels like a vanilla block of
     * {@code customHardness} when {@code preferredTool} matches, otherwise hand speed.
     * <p>
     * {@code attribute = (desiredSpeed / customHardness) / (vanillaSpeed / hostHardness)}
     */
    public static double attributeForCustomHardness(Block host, ItemStack tool,
                                                    double customHardness, boolean preferredTool) {
        if (customHardness <= 0) {
            return 0;
        }
        float hostHardness = getHostHardness(host);
        if (hostHardness <= 0) {
            // Tripwire / instant hosts cannot be time-scaled via attributes
            return preferredTool ? 1.0 : attributeForHandSpeed(host, tool);
        }

        // Preferred: speed as if mining a matching vanilla block (pickaxe→stone, axe→log, …)
        // so grade/Efficiency apply even when the host (note block) does not prefer this tool.
        double desiredSpeed = preferredTool ? getPreferredToolSpeed(tool) : 1.0;
        float vanillaSpeed = getVanillaToolSpeed(host, tool);
        return (desiredSpeed / customHardness) / (vanillaSpeed / hostHardness);
    }

    /**
     * Tool grade / destroy speed as if mining a typical block for that tool family
     * (stone for pickaxe, oak log for axe, dirt for shovel). Used when the host
     * does not treat the item as preferred (e.g. pickaxe vs note block).
     */
    public static float getPreferredToolSpeed(ItemStack tool) {
        if (tool == null || tool.getType().isAir()) {
            return 1.0f;
        }
        ToolItemType type = ToolItemType.valueOf(tool.getType());
        if (type == null) {
            return 1.0f;
        }
        Material template = switch (type) {
            case PICKAXE -> Material.STONE;
            case AXE -> Material.OAK_LOG;
            case SHOVEL -> Material.DIRT;
            case SHEARS -> Material.WHITE_WOOL;
            case HAND -> Material.STONE;
        };
        float speed = template.createBlockData().getDestroySpeed(tool, true);
        return speed <= 0 ? 1.0f : speed;
    }
}

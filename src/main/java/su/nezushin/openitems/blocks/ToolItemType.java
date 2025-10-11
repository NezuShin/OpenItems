package su.nezushin.openitems.blocks;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import com.google.common.collect.Sets;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public enum ToolItemType {
    HAND(new HashSet<>()), PICKAXE(MaterialTags.PICKAXES.getValues()), AXE(MaterialTags.AXES.getValues()), SHOVEL(MaterialTags.SHOVELS.getValues()),
    SHEARS(Sets.newHashSet(Material.SHEARS));

    private Set<Material> set;

    private ToolItemType(Set<Material> set) {
        this.set = set;
    }

    private static MaterialSetTag COPPER_TOOLS;

    static {
        try {
            COPPER_TOOLS = MaterialTags.COPPER_TOOLS;
        } catch (Throwable ignored) {

        }
    }


    /**
     * @param material tool's material
     * @return true if Material belongs this tool type
     */
    public boolean contains(Material material) {
        return set.contains(material);
    }

    /**
     * @param material tool's material
     * @return ToolItemType based on Material value
     */
    public static ToolItemType valueOf(Material material) {
        for (var i : ToolItemType.values())
            if (i.contains(material))
                return i;
        return null;
    }

    /**
     * Values taken from <a href="https://minecraft.wiki/w/Breaking">https://minecraft.wiki/w/Breaking</a>
     *
     * @param material Material of tool
     * @return dig speed multiplier
     */
    public static double getTypeModifier(Material material) {
        if (material == null || material.isAir())
            return 1.0;
        if (MaterialTags.WOODEN_TOOLS.isTagged(material))
            return 2.0;
        if (MaterialTags.STONE_TOOLS.isTagged(material))
            return 4.0;
        if (MaterialTags.IRON_TOOLS.isTagged(material))
            return 6.0;
        if (MaterialTags.DIAMOND_TOOLS.isTagged(material))
            return 8.0;
        if (MaterialTags.NETHERITE_TOOLS.isTagged(material))
            return 9.0;
        if (MaterialTags.GOLDEN_TOOLS.isTagged(material))
            return 12.0;
        if (COPPER_TOOLS != null && COPPER_TOOLS.isTagged(material))
            return 5.0;

        return 1.0;
    }

}

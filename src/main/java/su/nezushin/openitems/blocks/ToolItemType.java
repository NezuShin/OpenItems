package su.nezushin.openitems.blocks;

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

}

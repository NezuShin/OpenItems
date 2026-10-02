package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.types.CustomBlockModel;
import su.nezushin.openitems.utils.NBTUtil;

/**
 * Resolves the vanilla host {@link BlockState} WorldEdit should write before OpenItems applies its model.
 */
final class WorldEditHostStates {

    static BlockState forItem(ItemStack item) {
        return forItem(item, null);
    }

    static BlockState forItem(ItemStack item, String properties) {
        String modelId = NBTUtil.getBlockId(item);
        CustomBlockModel model = OpenItems.getInstance().getModelRegistry().getBlockTypes().get(modelId);
        if (model == null)
            throw new IllegalStateException("Unknown custom block model: " + modelId);

        Material hostMaterial = model.resolveHostMaterial(item);
        BlockData data = createHostBlockData(hostMaterial, properties);
        return BukkitAdapter.adapt(data);
    }

    static BlockState fromHostBlockData(ItemStack item, BlockData hostData) {
        String modelId = NBTUtil.getBlockId(item);
        CustomBlockModel model = OpenItems.getInstance().getModelRegistry().getBlockTypes().get(modelId);
        if (model == null)
            throw new IllegalStateException("Unknown custom block model: " + modelId);

        Material expectedMaterial = model.resolveHostMaterial(item);
        if (hostData.getMaterial() != expectedMaterial)
            throw new IllegalArgumentException(
                    "Host material " + hostData.getMaterial() + " does not match item material " + expectedMaterial
            );

        return BukkitAdapter.adapt(hostData);
    }

    private static BlockData createHostBlockData(Material material, String properties) {
        if (properties == null || properties.isBlank())
            return Bukkit.createBlockData(material);

        try {
            return Bukkit.createBlockData(material, properties);
        } catch (IllegalArgumentException first) {
            try {
                return Bukkit.createBlockData(material.getKey().getKey() + "[" + properties + "]");
            } catch (IllegalArgumentException ignored) {
                throw first;
            }
        }
    }
}

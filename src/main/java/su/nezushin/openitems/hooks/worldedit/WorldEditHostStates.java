package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.types.CustomBlockModel;
import su.nezushin.openitems.blocks.types.CustomChorusModel;
import su.nezushin.openitems.blocks.types.CustomNoteblockModel;
import su.nezushin.openitems.blocks.types.CustomSlabBlockModel;
import su.nezushin.openitems.blocks.types.CustomStairsBlockModel;
import su.nezushin.openitems.blocks.types.CustomTripwireModel;
import su.nezushin.openitems.utils.NBTUtil;

/**
 * Resolves the vanilla host {@link BlockState} WorldEdit should write before OpenItems applies its model.
 */
final class WorldEditHostStates {

    private WorldEditHostStates() {
    }

    static BlockState forItem(ItemStack item) {
        String modelId = NBTUtil.getBlockId(item);
        CustomBlockModel model = OpenItems.getInstance().getModelRegistry().getBlockTypes().get(modelId);
        if (model == null)
            throw new IllegalStateException("Unknown custom block model: " + modelId);

        Material hostMaterial = resolveHostMaterial(item, model);
        BlockData data = Bukkit.createBlockData(hostMaterial);
        return BukkitAdapter.adapt(data);
    }

    private static Material resolveHostMaterial(ItemStack item, CustomBlockModel model) {
        if (model instanceof CustomStairsBlockModel) {
            if (Tag.STAIRS.isTagged(item.getType()))
                return item.getType();
            return Material.OAK_STAIRS;
        }
        if (model instanceof CustomSlabBlockModel) {
            if (Tag.SLABS.isTagged(item.getType()))
                return item.getType();
            return Material.OAK_SLAB;
        }
        if (model instanceof CustomNoteblockModel)
            return Material.NOTE_BLOCK;
        if (model instanceof CustomTripwireModel)
            return Material.TRIPWIRE;
        if (model instanceof CustomChorusModel)
            return Material.CHORUS_PLANT;
        return Material.STONE;
    }
}

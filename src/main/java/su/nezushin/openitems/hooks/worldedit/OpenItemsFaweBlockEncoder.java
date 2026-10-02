package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.types.CustomArbitraryBlockModel;
import su.nezushin.openitems.blocks.types.CustomBlockModel;
import su.nezushin.openitems.blocks.types.CustomNoteblockModel;
import su.nezushin.openitems.blocks.types.CustomSlabBlockModel;
import su.nezushin.openitems.blocks.types.CustomStairsBlockModel;
import su.nezushin.openitems.utils.NBTUtil;

/**
 * Rewrites the {@link BlockStateHolder} FAWE queues so the flushed chunk already has the custom look.
 * Display entities are not spawned here; {@link OpenItemsFawePlacement} does that on the main thread.
 */
final class OpenItemsFaweBlockEncoder {

    private OpenItemsFaweBlockEncoder() {
    }

    record Encoded(BlockStateHolder<?> block, String overrideId, boolean spawnDisplay) {
    }

    static Encoded encode(BlockStateHolder<?> block, ItemStack item) {
        String modelId = NBTUtil.getBlockId(item);
        CustomBlockModel model = modelId == null
                ? null
                : OpenItems.getInstance().getModelRegistry().getBlockTypes().get(modelId);
        if (model == null)
            return passthrough(block);

        if (model instanceof CustomSlabBlockModel slab) {
            BlockData data = BukkitAdapter.adapt(block.toImmutableState());
            if (data instanceof Slab slabData && slabData.getType() == Slab.Type.DOUBLE)
                return encodeDoubleSlab(block, item, slab);
            return display(block);
        }

        if (model instanceof CustomStairsBlockModel || model instanceof CustomArbitraryBlockModel)
            return display(block);

        BlockData encoded = encodeState(block, model, item);
        if (encoded == null)
            return passthrough(block);

        return state(OpenItemsWorldEditTag.tag(BukkitAdapter.adapt(encoded), item));
    }

    private static Encoded encodeDoubleSlab(BlockStateHolder<?> original, ItemStack item, CustomSlabBlockModel slab) {
        String doubleId = slab.getDoubleNoteblockId();
        CustomBlockModel note = doubleId == null
                ? null
                : OpenItems.getInstance().getModelRegistry().getBlockTypes().get(doubleId);
        if (!(note instanceof CustomNoteblockModel noteModel))
            return passthrough(original);

        BlockData data = Bukkit.createBlockData(Material.NOTE_BLOCK);
        if (!noteModel.applyTo(data))
            return passthrough(original);
        return new Encoded(OpenItemsWorldEditTag.tag(BukkitAdapter.adapt(data), item), doubleId, false);
    }

    private static BlockData encodeState(BlockStateHolder<?> block, CustomBlockModel model, ItemStack item) {
        Material host = model.resolveHostMaterial(item);
        BlockData data = BukkitAdapter.adapt(block.toImmutableState());
        if (data.getMaterial() != host)
            data = Bukkit.createBlockData(host);
        if (!model.applyTo(data))
            return null;
        return data;
    }

    private static Encoded passthrough(BlockStateHolder<?> block) {
        return new Encoded(block, null, false);
    }

    private static Encoded display(BlockStateHolder<?> block) {
        return new Encoded(block, null, true);
    }

    private static Encoded state(BaseBlock block) {
        return new Encoded(block, null, false);
    }
}

package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinStringTag;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;
import su.nezushin.openitems.utils.NBTUtil;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Encodes and decodes OpenItems item payloads on WorldEdit {@link BaseBlock} NBT.
 */
final class OpenItemsWorldEditTag {

    private static final String ITEM_KEY = "openitems_item";

    private static final Map<BaseBlock, ItemStack> ITEM_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    static BaseBlock tag(BlockStateHolder<?> hostState, ItemStack item) {
        ItemStack prepared = prepareItem(item);
        String yaml = serializeItem(prepared);
        LinCompoundTag payload = LinCompoundTag.builder()
                .put(ITEM_KEY, LinStringTag.of(yaml))
                .build();
        BaseBlock tagged = hostState.toBaseBlock(LazyReference.from(() -> payload));
        ITEM_CACHE.put(tagged, prepared);
        return tagged;
    }

    static boolean representsSameBlock(BlockStateHolder<?> incoming, BlockLocationStore existing) {
        ItemStack incomingItem = extractItem(incoming);
        if (incomingItem == null)
            return false;
        String incomingId = NBTUtil.getBlockId(incomingItem);
        return incomingId != null && incomingId.equals(existing.getEffectiveBlockId());
    }

    static ItemStack extractItem(BlockStateHolder<?> block) {
        if (block instanceof BaseBlock baseBlock)
            return extractFromBaseBlock(baseBlock);
        return extractFromBaseBlock(block.toBaseBlock());
    }

    private static ItemStack extractFromBaseBlock(BaseBlock baseBlock) {
        ItemStack cached = ITEM_CACHE.get(baseBlock);
        if (cached != null)
            return cached.clone();

        LinCompoundTag root = baseBlock.getNbt();
        if (root == null)
            return null;

        var child = root.value().get(ITEM_KEY);
        if (!(child instanceof LinStringTag itemTag))
            return null;

        return deserializeItem(itemTag.value());
    }

    static ItemStack prepareItem(ItemStack item) {
        ItemStack copy = item.clone();
        copy.setAmount(1);
        return NBTUtil.clearOverrideId(copy);
    }

    private static String serializeItem(ItemStack item) {
        YamlConfiguration conf = new YamlConfiguration();
        conf.set("data", item);
        return conf.saveToString();
    }

    private static ItemStack deserializeItem(String yaml) {
        YamlConfiguration conf = new YamlConfiguration();
        try {
            conf.loadFromString(yaml);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException("Failed to deserialize OpenItems WorldEdit item payload", e);
        }
        return (ItemStack) conf.get("data");
    }


    static BaseBlock enrichFullBlock(World world, BlockVector3 position, BaseBlock block) {
        Block bukkitBlock = world.getBlockAt(position.x(), position.y(), position.z());
        BlockLocationStore store = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(bukkitBlock);
        if (store == null)
            return block;
        return OpenItemsWorldEditTag.tag(block, store.getCurrentItem());
    }
}

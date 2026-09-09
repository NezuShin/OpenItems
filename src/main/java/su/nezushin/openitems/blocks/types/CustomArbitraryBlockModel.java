package su.nezushin.openitems.blocks.types;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.utils.BlockEntityUtil;

/**
 * Arbitrary ItemDisplay overlay: host is the item's vanilla material; look is a
 * fixed {@code item_model} (the dropped JSON id). Host BlockData is ignored.
 */
public class CustomArbitraryBlockModel implements CustomBlockModel {

    public static final String SCOREBOARD_TAG = BlockEntityUtil.SCOREBOARD_TAG_PREFIX + "Arbitrary";

    private final String id;

    public CustomArbitraryBlockModel(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public void apply(Block b, boolean update) {
        var displayEntities = OpenItems.getInstance().getBlocks().getDisplayEntities();
        var item = createDisplayItem();
        var transformation = createTransformation();

        if (!displayEntities.containsKey(b)) {
            var location = b.getLocation().add(0.5, 0.5, 0.5);
            var display = b.getWorld().spawn(location, ItemDisplay.class, entity ->
                    BlockEntityUtil.configureBlockDisplay(entity, item, transformation, SCOREBOARD_TAG));
            displayEntities.put(b, display);
            return;
        }

        var display = displayEntities.get(b);
        if (display != null && display.isValid()) {
            display.teleport(b.getLocation().add(0.5, 0.5, 0.5));
            display.setTeleportDuration(0);
            display.setItemStack(item);
            display.setTransformation(transformation);
        } else {
            displayEntities.remove(b);
            apply(b, update);
        }
    }

    @Override
    public boolean isSimilar(Block b) {
        return true;
    }

    @Override
    public boolean applyOnPhysics() {
        return false;
    }

    @Override
    public boolean isReapplyOnLoadNeeded() {
        return true;
    }

    @Override
    public boolean denyVanillaRightClick() {
        return false;
    }

    @Override
    public Material resolveHostMaterial(ItemStack item) {
        return item.getType();
    }

    @Override
    public void remove(Block b) {
        var display = OpenItems.getInstance().getBlocks().getDisplayEntities().remove(b);
        if (display != null && display.isValid())
            display.remove();
    }

    public ItemStack createDisplayItem() {
        var item = new ItemStack(Material.STONE);
        var key = NamespacedKey.fromString(id);
        if (key != null)
            item.editMeta(meta -> meta.setItemModel(key));
        return item;
    }

    public static Transformation createTransformation() {
        return new Transformation(
                new Vector3f(),
                new Quaternionf(),
                new Vector3f(1f, 1f, 1f),
                new Quaternionf()
        );
    }
}

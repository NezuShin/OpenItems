package su.nezushin.openitems.blocks.types;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.utils.BlockEntityUtil;
import su.nezushin.openitems.utils.SlabPathsUtil;

/**
 * Half slabs: ItemDisplay on a vanilla slab host ({@code bottom}/{@code top}).
 * Doubles: promoted via {@link su.nezushin.openitems.blocks.CustomBlocks#overrideBlockModel}
 * to a note-block host; real {@code id} stays the slab model path.
 */
public class CustomSlabBlockModel implements CustomBlockModel {

    public static final String SCOREBOARD_TAG = BlockEntityUtil.SCOREBOARD_TAG_PREFIX + "Slab";

    private final String id;

    public CustomSlabBlockModel(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Paired note-block model id for the full (double) form.
     */
    public String getDoubleNoteblockId() {
        return SlabPathsUtil.toDoubleNoteblockId(id);
    }

    private static boolean shouldPromoteToDouble(Block block) {
        if (block.getType() == Material.NOTE_BLOCK)
            return true;
        return block.getBlockData() instanceof Slab slab && slab.getType() == Slab.Type.DOUBLE;
    }

    @Override
    public void apply(Block b, boolean update) {
        if (shouldPromoteToDouble(b)) {
            OpenItems.getInstance().getBlocks().overrideBlockModel(b, getDoubleNoteblockId());
            return;
        }
        if (!(b.getBlockData() instanceof Slab slab) || slab.getType() == Slab.Type.DOUBLE)
            return;

        var displayEntities = OpenItems.getInstance().getBlocks().getDisplayEntities();
        var item = createDisplayItem(slab);
        var transformation = createTransformation();

        if (!displayEntities.containsKey(b)) {
            var location = b.getLocation().add(0.5, 0.9, 0.5);
            var display = b.getWorld().spawn(location, ItemDisplay.class, entity -> {
                entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
                entity.setItemStack(item);
                entity.setTransformation(transformation);
                entity.addScoreboardTag(SCOREBOARD_TAG);
                entity.setPersistent(false);
            });
            displayEntities.put(b, display);
            return;
        }

        var display = displayEntities.get(b);
        if (display != null && display.isValid()) {
            display.teleport(b.getLocation().add(0.5, 0.9, 0.5));
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
        return b.getBlockData() instanceof Slab slab && slab.getType() != Slab.Type.DOUBLE;
    }

    @Override
    public boolean applyOnPhysics() {
        return true;
    }

    @Override
    public boolean isReapplyOnLoadNeeded() {
        return true;
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

    public ItemStack createDisplayItem(Slab slab) {
        var typeName = slab.getType() == Slab.Type.TOP ? "top" : "bottom";
        var modelPath = id + "/" + typeName;
        var item = new ItemStack(Material.STONE);
        var key = NamespacedKey.fromString(modelPath);
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

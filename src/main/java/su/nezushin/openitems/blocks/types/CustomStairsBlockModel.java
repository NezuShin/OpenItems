package su.nezushin.openitems.blocks.types;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.utils.BlockEntityUtil;

/**
 * Display stairs via ItemDisplay: 3 parent shapes (straight / inner / outer)
 * plus vanilla-equivalent x/y rotation applied on the entity.
 */
public class CustomStairsBlockModel implements CustomBlockModel {

    public static final String SCOREBOARD_TAG = BlockEntityUtil.SCOREBOARD_TAG_PREFIX + "Stairs";

    /**
     * ItemDisplay HEAD faces opposite of block models on the east/west axis;
     * add 180° Y on top of vanilla blockstate yaw (same fix as baked +180).
     */
    private static final int ITEM_DISPLAY_Y_OFFSET = 180;

    /**
     * Model HEAD translation is {@code [0, -6.4, 0]} (1/16-block units) while the
     * entity sits at {@code y + 0.9}. Visual center is 0.4 blocks below the entity;
     * rotate around that pivot so top-half (x=180) stays aligned.
     */
    private static final Vector3f ROTATION_PIVOT = new Vector3f(0f, -0.4f, 0f);

    private final String id;

    public CustomStairsBlockModel(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public void apply(Block b, boolean update) {
        if (!(b.getBlockData() instanceof Stairs stairs))
            return;

        var displayEntities = OpenItems.getInstance().getBlocks().getDisplayEntities();
        var item = createDisplayItem(stairs);
        var transformation = createTransformation(stairs);

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
        return Tag.STAIRS.isTagged(b.getType());
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

    public ItemStack createDisplayItem(Stairs stairs) {
        var modelPath = id + "/" + toShapeName(stairs.getShape());
        var item = new ItemStack(Material.STONE);
        var key = NamespacedKey.fromString(modelPath);
        if (key != null)
            item.editMeta(meta -> meta.setItemModel(key));
        return item;
    }

    public static Transformation createTransformation(Stairs stairs) {
        int rotX = stairs.getHalf() == Bisected.Half.TOP ? 180 : 0;
        int rotY = (vanillaYaw(stairs) + ITEM_DISPLAY_Y_OFFSET) % 360;

        var leftRotation = new Quaternionf()
                .rotateY((float) Math.toRadians(rotY))
                .rotateX((float) Math.toRadians(rotX));

        // T = P + R*(-P) so leftRotation orbits the visual block center
        var translation = new Vector3f(ROTATION_PIVOT)
                .negate()
                .rotate(leftRotation)
                .add(ROTATION_PIVOT);

        return new Transformation(
                translation,
                leftRotation,
                new Vector3f(1f, 1f, 1f),
                new Quaternionf()
        );
    }

    /**
     * Parent model name for the shape. Left/right variants share a parent;
     * facing/half are handled by {@link #createTransformation(Stairs)}.
     */
    public static String toShapeName(Stairs.Shape shape) {
        return switch (shape) {
            case INNER_LEFT, INNER_RIGHT -> "inner";
            case OUTER_LEFT, OUTER_RIGHT -> "outer";
            default -> "straight";
        };
    }

    /**
     * Vanilla {@code acacia_stairs} blockstate Y rotation for this facing/half/shape.
     * Matches Minecraft's baked blockstate table (X is only 0 or 180 from half).
     */
    public static int vanillaYaw(Stairs stairs) {
        BlockFace facing = stairs.getFacing();
        boolean top = stairs.getHalf() == Bisected.Half.TOP;
        Stairs.Shape shape = stairs.getShape();

        if (!top) {
            return switch (facing) {
                case EAST -> switch (shape) {
                    case STRAIGHT, INNER_RIGHT, OUTER_RIGHT -> 0;
                    case INNER_LEFT -> 90;
                    case OUTER_LEFT -> 270;
                };
                case NORTH -> switch (shape) {
                    case STRAIGHT -> 90;
                    case INNER_RIGHT, OUTER_RIGHT -> 90;
                    case INNER_LEFT, OUTER_LEFT -> 180;
                };
                case SOUTH -> switch (shape) {
                    case STRAIGHT, INNER_RIGHT, OUTER_RIGHT -> 270;
                    case INNER_LEFT -> 0;
                    case OUTER_LEFT -> 180;
                };
                case WEST -> switch (shape) {
                    case STRAIGHT, OUTER_RIGHT, INNER_RIGHT -> 180;
                    case INNER_LEFT -> 270;
                    case OUTER_LEFT -> 90;
                };
                default -> 0;
            };
        }

        // half=top: vanilla remaps yaw because x=180 mirrors left/right
        return switch (facing) {
            case EAST -> switch (shape) {
                case STRAIGHT, INNER_LEFT -> 90;
                case INNER_RIGHT, OUTER_RIGHT -> 0;
                case OUTER_LEFT -> 270;
            };
            case NORTH -> switch (shape) {
                case STRAIGHT, INNER_LEFT, OUTER_LEFT -> 180;
                case INNER_RIGHT, OUTER_RIGHT -> 90;
            };
            case SOUTH -> switch (shape) {
                case STRAIGHT -> 0;
                case INNER_RIGHT, OUTER_RIGHT -> 270;
                case INNER_LEFT, OUTER_LEFT -> 180;
            };
            case WEST -> switch (shape) {
                case STRAIGHT, INNER_LEFT, OUTER_LEFT -> 270;
                case OUTER_RIGHT, INNER_RIGHT -> 180;
            };
            default -> 0;
        } + 270;
    }
}

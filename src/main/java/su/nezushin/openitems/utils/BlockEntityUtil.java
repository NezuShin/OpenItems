package su.nezushin.openitems.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;

public class BlockEntityUtil {

    public static final String SCOREBOARD_TAG_PREFIX = "OI_BLOCK_";

    public static boolean hasBlockDisplayTag(Entity entity) {
        for (var tag : entity.getScoreboardTags()) {
            if (tag.startsWith(SCOREBOARD_TAG_PREFIX))
                return true;
        }
        return false;
    }

    public static void configureBlockDisplay(ItemDisplay entity, ItemStack item, Transformation transformation, String scoreboardTag) {
        entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
        entity.setItemStack(item);
        entity.setTransformation(transformation);
        entity.addScoreboardTag(scoreboardTag);
        entity.setPersistent(false);
    }
}

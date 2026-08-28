package su.nezushin.openitems.utils;

import org.bukkit.entity.Entity;

public class BlockEntityUtil {

    public static final String SCOREBOARD_TAG_PREFIX = "OI_BLOCK_";

    public static boolean hasBlockDisplayTag(Entity entity) {
        for (var tag : entity.getScoreboardTags()) {
            if (tag.startsWith(SCOREBOARD_TAG_PREFIX))
                return true;
        }
        return false;
    }
}

package su.nezushin.openitems.utils;

import org.bukkit.NamespacedKey;

public final class SlabPathsUtil {

    public static final String TEXTURE_PREFIX = "block/item_display/slabs/";
    public static final String DOUBLE_NOTEBLOCK_PREFIX = "block/note_block/double_slabs/";

    public static String toDoubleNoteblockPath(String slabModelPath) {
        if (!slabModelPath.startsWith(TEXTURE_PREFIX))
            return null;
        return DOUBLE_NOTEBLOCK_PREFIX + slabModelPath.substring(TEXTURE_PREFIX.length());
    }

    public static String toDoubleNoteblockId(String slabModelId) {
        var key = NamespacedKey.fromString(slabModelId);
        if (key == null)
            return null;
        var doublePath = toDoubleNoteblockPath(key.getKey());
        if (doublePath == null)
            return null;
        return key.getNamespace() + ":" + doublePath;
    }

    public static boolean isSlabModelId(String modelId) {
        var key = NamespacedKey.fromString(modelId);
        if (key == null)
            return false;
        return key.getKey().startsWith(TEXTURE_PREFIX);
    }
}

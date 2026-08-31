package su.nezushin.openitems.hooks.worldedit;

import org.bukkit.Bukkit;

/**
 * Runtime flags for which WorldEdit integration tier is active.
 */
public final class WorldEditSupportState {

    private static volatile boolean basic;
    private static volatile boolean extended;
    private static volatile boolean faweMode;

    public static void configure(boolean basicSupport, boolean extendedSupport, boolean faweIntegration) {
        basic = basicSupport;
        extended = extendedSupport;
        faweMode = faweIntegration;
    }

    public static void disable() {
        basic = false;
        extended = false;
        faweMode = false;
    }

    public static boolean isBasicActive() {
        return basic;
    }

    public static boolean isExtendedActive() {
        return extended;
    }

    public static boolean isFaweMode() {
        return faweMode;
    }

    /** Vanilla WE extended path: wrap {@code EditSession.world} for snapshot reads. */
    public static boolean shouldWrapWorld() {
        return extended && !faweMode;
    }

    public static boolean isWorldEditPresent() {
        return Bukkit.getPluginManager().isPluginEnabled("WorldEdit")
                || Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit");
    }

    public static boolean isFawePresent() {
        if (!Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit"))
            return false;
        try {
            Class.forName("com.fastasyncworldedit.core.Fawe");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

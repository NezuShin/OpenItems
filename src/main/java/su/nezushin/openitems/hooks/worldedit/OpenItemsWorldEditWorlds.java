package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.world.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

/**
 * Caches wrapped WorldEdit worlds per Bukkit world.
 */
public final class OpenItemsWorldEditWorlds {

    private static final Map<org.bukkit.World, OpenItemsWorldEditWorld> CACHE = new ConcurrentHashMap<>();

    private OpenItemsWorldEditWorlds() {
    }

    public static World wrap(World world, BooleanSupplier active) {
        if (world == null)
            return null;
        if (isWrapped(world))
            return world;
        if (!(world instanceof BukkitWorld))
            return world;

        org.bukkit.World bukkitWorld = BukkitAdapter.adapt(world);
        return CACHE.computeIfAbsent(bukkitWorld, key -> new OpenItemsWorldEditWorld(key, active));
    }

    public static boolean isWrapped(World world) {
        return world instanceof OpenItemsWorldEditWorld;
    }

    public static void clearCache() {
        CACHE.clear();
    }
}

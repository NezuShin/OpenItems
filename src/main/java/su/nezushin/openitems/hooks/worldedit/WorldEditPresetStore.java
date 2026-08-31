package su.nezushin.openitems.hooks.worldedit;

import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * In-memory WorldEdit block presets per player ({@code //set oi:preset:<name>}).
 */
public final class WorldEditPresetStore {

    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_-]+");

    private final Map<UUID, Map<String, ItemStack>> presets = new ConcurrentHashMap<>();

    public void save(UUID player, String name, ItemStack item) {
        String key = normalizeName(name);
        presets.computeIfAbsent(player, ignored -> new ConcurrentHashMap<>())
                .put(key, OpenItemsWorldEditTag.prepareItem(item));
    }

    public ItemStack get(UUID player, String name) {
        Map<String, ItemStack> playerPresets = presets.get(player);
        if (playerPresets == null)
            return null;

        ItemStack item = playerPresets.get(normalizeName(name));
        return item == null ? null : item.clone();
    }

    public boolean delete(UUID player, String name) {
        Map<String, ItemStack> playerPresets = presets.get(player);
        if (playerPresets == null)
            return false;
        return playerPresets.remove(normalizeName(name)) != null;
    }

    public Set<String> getNames(UUID player) {
        Map<String, ItemStack> playerPresets = presets.get(player);
        if (playerPresets == null || playerPresets.isEmpty())
            return Set.of();
        return Collections.unmodifiableSet(playerPresets.keySet());
    }

    public void clearAll() {
        presets.clear();
    }

    public static String normalizeName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Preset name is empty");
        if (!NAME_PATTERN.matcher(name).matches())
            throw new IllegalArgumentException("Invalid preset name: " + name);
        return name;
    }
}

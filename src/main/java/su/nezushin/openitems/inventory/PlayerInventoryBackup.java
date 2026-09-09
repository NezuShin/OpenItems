package su.nezushin.openitems.inventory;

import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory player inventory snapshots taken before each {@code /oi inv load}.
 * Kept until server restart so restore can be used more than once.
 */
public final class PlayerInventoryBackup {

    private final Map<UUID, ItemStack[]> backups = new ConcurrentHashMap<>();

    public void put(UUID player, ItemStack[] contents) {
        backups.put(player, SavedInventoryStore.cloneContents(contents));
    }

    public ItemStack[] get(UUID player) {
        ItemStack[] snapshot = backups.get(player);
        return snapshot == null ? null : SavedInventoryStore.cloneContents(snapshot);
    }
}

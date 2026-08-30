package su.nezushin.openitems.blocks.storage.chunk;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ItemPalette {

    private final List<String> serializedItems = new ArrayList<>();
    private final Map<String, Integer> indexBySerialized = new HashMap<>();

    List<String> getSerializedItems() {
        return serializedItems;
    }

    void setSerializedItems(List<String> items) {
        serializedItems.clear();
        indexBySerialized.clear();
        if (items == null)
            return;
        for (String item : items)
            intern(item);
    }

    /** Returns existing index or appends serialized item. */
    int intern(String serializedItem) {
        Integer existing = indexBySerialized.get(serializedItem);
        if (existing != null)
            return existing;
        int index = serializedItems.size();
        serializedItems.add(serializedItem);
        indexBySerialized.put(serializedItem, index);
        return index;
    }

    /** Deserialize palette entry → ItemStack clone. */
    ItemStack resolve(int index) {
        return ItemStackYamlSerializer.deserialize(serializedItems.get(index)).clone();
    }
}

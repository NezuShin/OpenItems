package su.nezushin.openitems;

import su.nezushin.openitems.blocks.types.CustomBlockModel;

import java.util.*;

public class ModelRegistry {
    private Set<String> items = new HashSet<>();

    private Map<String, CustomBlockModel> blockTypes = new HashMap<>();

    public Map<String, CustomBlockModel> getBlockTypes() {
        return blockTypes;
    }

    public Set<String> getItems() {
        return items;
    }

    public void clear() {
        items.clear();
        blockTypes.clear();
    }
}

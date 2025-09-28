package su.nezushin.openitems;

import su.nezushin.openitems.blocks.types.CustomBlockType;

import java.util.*;

public class ModelRegistry {
    private Set<String> items = new HashSet<>();

    private Map<String, CustomBlockType> blockTypes = new HashMap<>();

    public Map<String, CustomBlockType> getBlockTypes() {
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

package su.nezushin.openitems;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import su.nezushin.openitems.blocks.types.CustomBlockModel;
import su.nezushin.openitems.blocks.types.CustomChorusModel;
import su.nezushin.openitems.blocks.types.CustomNoteblockModel;
import su.nezushin.openitems.blocks.types.CustomTripwireModel;
import su.nezushin.openitems.utils.Message;

import java.util.*;

/**
 * Registry for the item models, block models, equipment and font images
 */
public class ModelRegistry {

    //everything from items in all namespaces
    private Set<String> items = new HashSet<>();

    //armor models
    private Set<String> equipment = new HashSet<>();

    //block model id's taken from build/assets/minecraft/blockstates/note_block.json and tripwire.json
    private Map<String, CustomBlockModel> blockTypes = new HashMap<>();

    //font images, key is emoji id, value is the symbol
    private Map<String, String> fontImages = new HashMap<>();


    //font spaces
    private SortedMap<Integer, String> fontSpaces = new TreeMap<>();

    public Map<String, CustomBlockModel> getBlockTypes() {
        return blockTypes;
    }

    public Set<String> getItems() {
        return items;
    }


    public Set<String> getEquipment() {
        return equipment;
    }

    public Map<String, String> getFontImages() {
        return fontImages;
    }

    public Map<Integer, String> getFontSpaces() {
        return fontSpaces;
    }

    //synchronized collections for async resource pack scan and build

    /**
     * Sets collections synchronized for building time. After building, builder should set it back to unmodifiable.
     * @param lock set all collections unmodifiable
     */
    public void setLock(boolean lock) {
        if (lock) {
            items = Collections.unmodifiableSet(new HashSet<>(items));
            equipment = Collections.unmodifiableSet(new HashSet<>(equipment));
            blockTypes = Collections.unmodifiableMap(new HashMap<>(blockTypes));
            fontImages = Collections.unmodifiableMap(new HashMap<>(fontImages));
            fontSpaces = Collections.unmodifiableSortedMap(new TreeMap<>(fontSpaces));

            reportLoaded(Bukkit.getConsoleSender());
            return;
        }
        items = Collections.synchronizedSet(new HashSet<>(items));
        equipment = Collections.synchronizedSet(new HashSet<>(equipment));
        blockTypes = Collections.synchronizedMap(new HashMap<>(blockTypes));
        fontImages = Collections.synchronizedMap(new HashMap<>(fontImages));
        fontSpaces = Collections.synchronizedSortedMap(new TreeMap<>(fontSpaces));
    }


    public void reportLoaded(CommandSender sender) {
        Message.registry_loaded.replace("{items}", String.valueOf(items.size()),
                "{equipment}", String.valueOf(equipment.size()),
                "{block-types-total}", String.valueOf(blockTypes.size()),
                "{block-types-tripwire}", String.valueOf(blockTypes.values().stream()
                        .filter(i -> i instanceof CustomTripwireModel).count()),
                "{block-types-noteblock}", String.valueOf(blockTypes.values().stream()
                        .filter(i -> i instanceof CustomNoteblockModel).count()),
                "{block-types-chorus}", String.valueOf(blockTypes.values().stream()
                        .filter(i -> i instanceof CustomChorusModel).count()),
                "{font-images}", String.valueOf(fontImages.size()),
                "{font-spaces}", String.valueOf(fontSpaces.size())
        ).send(sender);
    }

    public void clear() {
        items = new HashSet<>();
        equipment = new HashSet<>();
        blockTypes = new HashMap<>();
        fontSpaces = new TreeMap<>();
        fontImages = new HashMap<>();
    }
}

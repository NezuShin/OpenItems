package su.nezushin.openitems.blocks.storage.chunk;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import su.nezushin.openitems.OpenItems;

import java.util.HashMap;
import java.util.Map;

final class BlockExtraCodec {

    /** Empty map → null (omit "m" on disk). */
    static String encode(Map<String, Object> extras) {
        if (extras == null || extras.isEmpty())
            return null;

        YamlConfiguration conf = new YamlConfiguration();
        for (var e : extras.entrySet())
            conf.set(e.getKey(), e.getValue());
        return conf.saveToString();
    }

    /** null/blank → empty map. Corrupt YAML → log warning, empty map. */
    static Map<String, Object> decode(String yaml) {
        if (yaml == null || yaml.isBlank())
            return Map.of();

        YamlConfiguration conf = new YamlConfiguration();
        try {
            conf.loadFromString(yaml);
        } catch (InvalidConfigurationException e) {
            OpenItems.getInstance().getLogger().warning(
                    "Failed to decode block extras YAML, treating as empty: " + e.getMessage());
            return Map.of();
        }
        return new HashMap<>(conf.getValues(false));
    }
}

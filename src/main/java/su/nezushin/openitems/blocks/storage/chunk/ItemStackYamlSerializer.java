package su.nezushin.openitems.blocks.storage.chunk;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

final class ItemStackYamlSerializer {

    static String serialize(ItemStack item) {
        YamlConfiguration conf = new YamlConfiguration();
        conf.set("data", item);
        return conf.saveToString();
    }

    static ItemStack deserialize(String yaml) {
        YamlConfiguration conf = new YamlConfiguration();
        try {
            conf.loadFromString(yaml);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException("Failed to deserialize palette item", e);
        }
        return (ItemStack) conf.get("data");
    }
}

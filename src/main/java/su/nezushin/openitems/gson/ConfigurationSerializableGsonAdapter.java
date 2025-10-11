package su.nezushin.openitems.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;


/**
 * Adapter needed to save any arbitrary data that implements ConfigurationSerializable (ItemStacks, Locations, etc)
 */
public class ConfigurationSerializableGsonAdapter extends TypeAdapter<ConfigurationSerializable> {
    @Override
    public void write(JsonWriter out, ConfigurationSerializable value) throws IOException {
        var yamlConf = new YamlConfiguration();

        yamlConf.set("data", value);

        out.value(yamlConf.saveToString());
    }

    @Override
    public ConfigurationSerializable read(JsonReader in) throws IOException {
        var yamlConf = new YamlConfiguration();

        try {
            yamlConf.loadFromString(in.nextString());

            return (ConfigurationSerializable) yamlConf.get("data");
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return Gson parser that can save/load every ConfigurationSerializable-based data. e.g. ItemStacks
     */
    public static Gson createGson() {
        return new GsonBuilder().
                registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ConfigurationSerializableGsonAdapter())
                .create();
    }
}

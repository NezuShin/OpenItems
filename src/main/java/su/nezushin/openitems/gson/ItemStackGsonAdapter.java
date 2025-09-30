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

public class ItemStackGsonAdapter extends TypeAdapter<ItemStack> {
    @Override
    public void write(JsonWriter out, ItemStack value) throws IOException {
        var yamlConf = new YamlConfiguration();

        yamlConf.set("data", value);

        out.value(yamlConf.saveToString());
    }

    @Override
    public ItemStack read(JsonReader in) throws IOException {
        var yamlConf = new YamlConfiguration();

        try {
            yamlConf.loadFromString(in.nextString());

            return yamlConf.getItemStack("data");
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Gson createGson() {
        return new GsonBuilder().
                registerTypeHierarchyAdapter(ItemStack.class, new ItemStackGsonAdapter())
                .create();
    }
}

package su.nezushin.openitems.rp.cache;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.rp.font.FontImage;
import su.nezushin.openitems.rp.font.FontImageProviders;
import su.nezushin.openitems.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to store and assign new ids (symbols) for blocks. Assigns Private Use Area symbols (starts from \uE000)
 */
public class FontImagesIdCache extends JsonCache {

    private Map<String, Integer> charIds = new HashMap<>();
    private Map<String, FontImage> registeredCharIds = new HashMap<>();

    private int nextCharId = Utils.unicodeEscapeSequenceToInt("\\uE000");

    public int getOrCreateFontImageId(String name) {
        var id = charIds.get(name);

        if (id == null) {
            id = nextCharId++;
            charIds.put(name, id);
        }

        return id;
    }

    public void build() throws IOException {
        var fontsDir = new File(OpenItems.getInstance().getDataFolder(), "build/assets/minecraft/font");

        fontsDir.mkdirs();

        var data = new Gson().toJson(new FontImageProviders(new ArrayList<>(registeredCharIds.values())));

        for (var file : new File[]{
                new File(fontsDir, "default.json"),
                new File(fontsDir, "uniform.json")
        })
            Files.writeString(file.toPath(), data, Charsets.UTF_8);
    }

    public Map<String, FontImage> getRegisteredCharIds() {
        return registeredCharIds;
    }

    @Override
    protected String getName() {
        return "font-images-cache";
    }

    public void cleanRegistered() {
        this.registeredCharIds = new HashMap<>();
    }
}

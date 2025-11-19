package su.nezushin.openitems.rp.cache;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.rp.font.BitmapFontImage;
import su.nezushin.openitems.rp.font.FontImage;
import su.nezushin.openitems.rp.font.FontImageProviders;
import su.nezushin.openitems.rp.font.SpaceFontImage;
import su.nezushin.openitems.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to store and assign new ids (symbols) for blocks. Assigns Private Use Area symbols (starts from \uE000)
 */
public class FontImageIdCache extends JsonCache {

    private Map<String, Integer> charIds = new HashMap<>();
    private Map<String, BitmapFontImage> registeredCharIds = new HashMap<>();
    private Map<Integer, String> fontSpaces = new HashMap<>();

    private int nextCharId = Utils.unicodeEscapeSequenceToInt("\\uE000");

    public int getOrCreateFontImageId(String name) {
        var id = charIds.get(name);

        if (id == null) {
            id = nextCharId++;
            charIds.put(name, id);
        }

        return id;
    }

    private void addSpace(int offset, String str, SpaceFontImage image){
        image.getAdvances().put(str, offset);
        fontSpaces.put(offset, str);
    }

    private SpaceFontImage prepareSpaces() {
        var image = new SpaceFontImage();
        var charId = Utils.unicodeEscapeSequenceToInt("\\uF8FF");//take last code point in range

        for (var i = 1; i <= 512; i *= 2) {
            addSpace(i, String.valueOf((char)charId--), image);
            addSpace(-i, String.valueOf((char)charId--), image);
        }
        return image;
    }

    public void build() throws IOException {
        var fontsDir = new File(OpenItems.getInstance().getDataFolder(), "build/assets/minecraft/font");

        fontsDir.mkdirs();

        var array = new ArrayList<FontImage>(registeredCharIds.values());


        array.add(prepareSpaces());

        var data = OpenItems.getInstance().getGson().toJson(new FontImageProviders(array));

        for (var file : new File[]{
                new File(fontsDir, "default.json"),
                new File(fontsDir, "uniform.json")
        })
            Files.writeString(file.toPath(), data, Charsets.UTF_8);
    }

    public Map<Integer, String> getFontSpaces() {
        return fontSpaces;
    }

    public Map<String, BitmapFontImage> getRegisteredCharIds() {
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

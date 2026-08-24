package su.nezushin.openitems.rp.cache;

import com.google.common.base.Charsets;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Used to store and assign new ids (symbols) for font images. Assigns Private Use Area symbols (starts from \uE000).
 * Also builds per-font provider JSON files under {@code build/assets/<namespace>/font/}.
 */
public class FontImageIdCache extends JsonCache {

    private Map<String, Integer> charIds = new HashMap<>();
    private Map<String, BitmapFontImage> registeredCharIds = new HashMap<>();
    /**
     * Texture path → native font id ({@code namespace:fontName}), or {@code minecraft:default} in legacy mode.
     */
    private Map<String, String> imageFonts = new HashMap<>();
    private Map<Integer, String> fontSpaces = new HashMap<>();

    private transient List<FontPlacement> placements = new ArrayList<>();

    private int nextCharId = Utils.unicodeEscapeSequenceToInt("\\uE000");

    private record FontPlacement(BitmapFontImage image, String nativeFont, List<String> mergeInto,
                                 boolean appendSpaces) {
    }

    public int getOrCreateFontImageId(String name) {
        var id = charIds.get(name);

        if (id == null) {
            id = nextCharId++;
            charIds.put(name, id);
        }

        return id;
    }

    /**
     * Register a glyph for pack generation.
     *
     * @param path         registry / texture id (e.g. {@code foo:font/hud/icon})
     * @param data         bitmap provider data
     * @param nativeFont   font file to write under this namespace ({@code foo:hud}), or {@code null} in legacy mode
     * @param mergeInto    additional fonts to inject into (e.g. {@code minecraft:default})
     * @param appendSpaces whether to include the space provider on target fonts
     */
    public void register(String path, BitmapFontImage data, String nativeFont, List<String> mergeInto,
                         boolean appendSpaces) {
        this.registeredCharIds.put(path, data);
        this.imageFonts.put(path, nativeFont != null ? nativeFont : "minecraft:default");
        if (this.placements == null)
            this.placements = new ArrayList<>();
        this.placements.add(new FontPlacement(data, nativeFont,
                mergeInto == null ? List.of() : List.copyOf(mergeInto), appendSpaces));
    }

    private void addSpace(int offset, String str, SpaceFontImage image) {
        image.getAdvances().put(str, offset);
        fontSpaces.put(offset, str);
    }

    private SpaceFontImage prepareSpaces() {
        var image = new SpaceFontImage();
        var charId = Utils.unicodeEscapeSequenceToInt("\\uF8FF");//take last code point in range

        for (var i = 1; i <= 512; i *= 2) {
            addSpace(i, String.valueOf((char) charId--), image);
            addSpace(-i, String.valueOf((char) charId--), image);
        }
        return image;
    }

    public void build() throws IOException {
        if (this.placements == null)
            this.placements = new ArrayList<>();

        this.fontSpaces = new HashMap<>();
        var spaces = prepareSpaces();

        Map<String, LinkedHashSet<BitmapFontImage>> bitmapsByFont = new LinkedHashMap<>();
        Map<String, Boolean> spacesByFont = new HashMap<>();

        for (var placement : this.placements) {
            Set<String> targets = new LinkedHashSet<>();
            if (placement.nativeFont() != null)
                targets.add(placement.nativeFont());
            targets.addAll(placement.mergeInto());

            for (var fontId : targets) {
                bitmapsByFont.computeIfAbsent(fontId, k -> new LinkedHashSet<>()).add(placement.image());
                if (placement.appendSpaces())
                    spacesByFont.put(fontId, true);
            }
        }

        // Offsets must keep working for normal chat / unicode font setting
        bitmapsByFont.computeIfAbsent("minecraft:default", k -> new LinkedHashSet<>());
        bitmapsByFont.computeIfAbsent("minecraft:uniform", k -> new LinkedHashSet<>());
        spacesByFont.put("minecraft:default", true);
        spacesByFont.put("minecraft:uniform", true);

        for (var entry : bitmapsByFont.entrySet()) {
            var providers = new ArrayList<FontImage>(entry.getValue());
            if (Boolean.TRUE.equals(spacesByFont.get(entry.getKey())))
                providers.add(spaces);
            writeFontFile(entry.getKey(), providers);
        }
    }

    private void writeFontFile(String fontId, List<FontImage> providers) throws IOException {
        var colon = fontId.indexOf(':');
        if (colon <= 0 || colon == fontId.length() - 1)
            throw new IOException("Invalid font id '" + fontId + "' (expected namespace:name)");

        var namespace = fontId.substring(0, colon);
        var name = fontId.substring(colon + 1);
        var fontsDir = new File(OpenItems.getInstance().getDataFolder(), "build/assets/" + namespace + "/font");
        fontsDir.mkdirs();

        var data = OpenItems.getInstance().getGson().toJson(new FontImageProviders(providers));
        Files.writeString(new File(fontsDir, name + ".json").toPath(), data, Charsets.UTF_8);
    }

    public Map<Integer, String> getFontSpaces() {
        return fontSpaces;
    }

    public Map<String, BitmapFontImage> getRegisteredCharIds() {
        return registeredCharIds;
    }

    public Map<String, String> getImageFonts() {
        return imageFonts;
    }

    @Override
    protected String getName() {
        return "font-images-cache";
    }

    public void cleanRegistered() {
        this.registeredCharIds = new HashMap<>();
        this.imageFonts = new HashMap<>();
        this.placements = new ArrayList<>();
    }
}

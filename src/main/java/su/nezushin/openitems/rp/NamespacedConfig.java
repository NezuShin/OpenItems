package su.nezushin.openitems.rp;

import com.google.common.collect.Lists;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.codehaus.plexus.util.FileUtils;
import su.nezushin.openitems.rp.font.BitmapFontImage;
import su.nezushin.openitems.rp.sound.Sound;
import su.nezushin.openitems.rp.sound.SoundEvent;
import su.nezushin.openitems.rp.textures.TextureLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents config for every namespcace; All yml configs for namcespace should be located in OpenItems/contents/&lt;namespace&gt;/configs/
 */
public class NamespacedConfig {

    private boolean allowAutogen = true;

    private String namespace;

    private List<String> autogenIgnoreList = new ArrayList<>();


    private List<String> extensionsIgnoreList = Lists.newArrayList(".yml");
    private List<String> directoriesIgnoreList = Lists.newArrayList();


    private String generatedModelTemplate = "{\"parent\":\"minecraft:item/generated\",\"textures\":{\"layer0\":\"{path}\"}}";
    private String handheldModelTemplate = "{\"parent\":\"minecraft:item/handheld\",\"textures\":{\"layer0\":\"{path}\"}}";
    private String bowModelTemplate = "{\"parent\":\"minecraft:item/bow\",\"textures\":{\"layer0\":\"{path}\"}";

    private String cubeAllModelTemplate = "{\"parent\": \"minecraft:block/cube_all\",\"textures\": {\"all\": \"{path}\"}}";


    private String cubeModelTemplate = "{\"parent\": \"minecraft:block/cube\",\"textures\": {\"down\": \"{path_down}\",\"east\": \"{path_east}\",\"north\": \"{path_north}\",\"particle\": \"{path_up}\",\"south\": \"{path_south}\",\"up\": \"{path_up}\",\"west\": \"{path_west}\"}}";

    private String cubeSideModelTemplate = "{\"parent\": \"minecraft:block/cube\",\"textures\": {\"down\": \"{path_down}\",\"east\": \"{path_side}\",\"north\": \"{path_side}\",\"particle\": \"{path_up}\",\"south\": \"{path_side}\",\"up\": \"{path_up}\",\"west\": \"{path_side}\"}}";

    // display stairs shapes (full models, no parent)
    // {path_bottom}/{path_side}/{path_top}; particle uses side. single-texture fills all three with same id
    private String stairsStraightModelTemplate = "{\"textures\":{\"particle\":\"{path_side}\",\"bottom\":\"{path_bottom}\",\"side\":\"{path_side}\",\"top\":\"{path_top}\"},\"elements\":[{\"from\":[-0.02,-0.02,-0.02],\"to\":[16.02,8.02,16.02],\"faces\":{\"down\":{\"uv\":[0,0,16,16],\"texture\":\"#bottom\",\"cullface\":\"down\"},\"up\":{\"uv\":[0,0,16,16],\"texture\":\"#top\"},\"north\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"north\"},\"south\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"south\"},\"west\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"west\"},\"east\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"east\"}}},{\"from\":[7.98,8.02,-0.02],\"to\":[16.02,16.02,16.02],\"faces\":{\"up\":{\"uv\":[8,0,16,16],\"texture\":\"#top\",\"cullface\":\"up\"},\"north\":{\"uv\":[0,0,8,8],\"texture\":\"#side\",\"cullface\":\"north\"},\"south\":{\"uv\":[8,0,16,8],\"texture\":\"#side\",\"cullface\":\"south\"},\"west\":{\"uv\":[0,0,16,8],\"texture\":\"#side\"},\"east\":{\"uv\":[0,0,16,8],\"texture\":\"#side\",\"cullface\":\"east\"}}}],\"display\":{\"head\":{\"rotation\":[0,0,0],\"translation\":[0,-6.4,0],\"scale\":[1,1,1]}}}";

    private String stairsInnerModelTemplate = "{\"textures\":{\"particle\":\"{path_side}\",\"bottom\":\"{path_bottom}\",\"side\":\"{path_side}\",\"top\":\"{path_top}\"},\"elements\":[{\"from\":[-0.02,-0.02,-0.02],\"to\":[16.02,8.02,16.02],\"faces\":{\"north\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"north\"},\"east\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"east\"},\"south\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"south\"},\"west\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"west\"},\"up\":{\"uv\":[0,0,16,16],\"texture\":\"#top\"},\"down\":{\"uv\":[0,0,16,16],\"texture\":\"#bottom\",\"cullface\":\"down\"}}},{\"from\":[7.98,8.02,-0.02],\"to\":[16.02,16.02,16.02],\"faces\":{\"north\":{\"uv\":[0,0,8,8],\"texture\":\"#side\",\"cullface\":\"north\"},\"east\":{\"uv\":[0,0,16,8],\"texture\":\"#side\",\"cullface\":\"east\"},\"south\":{\"uv\":[8,0,16,8],\"texture\":\"#side\",\"cullface\":\"south\"},\"west\":{\"uv\":[0,0,16,8],\"texture\":\"#side\"},\"up\":{\"uv\":[8,0,16,16],\"texture\":\"#top\",\"cullface\":\"up\"}}},{\"from\":[-0.02,8.02,7.98],\"to\":[8.02,16.02,16.02],\"faces\":{\"north\":{\"uv\":[8,0,16,8],\"texture\":\"#side\"},\"south\":{\"uv\":[0,0,8,8],\"texture\":\"#side\",\"cullface\":\"south\"},\"west\":{\"uv\":[8,0,16,8],\"texture\":\"#side\",\"cullface\":\"west\"},\"up\":{\"uv\":[0,8,8,16],\"texture\":\"#top\",\"cullface\":\"up\"}}}],\"display\":{\"head\":{\"translation\":[0,-6.4,0]}}}";

    private String stairsOuterModelTemplate = "{\"textures\":{\"particle\":\"{path_side}\",\"bottom\":\"{path_bottom}\",\"side\":\"{path_side}\",\"top\":\"{path_top}\"},\"elements\":[{\"from\":[-0.02,-0.02,-0.02],\"to\":[16.02,8.02,16.02],\"faces\":{\"down\":{\"uv\":[0,0,16,16],\"texture\":\"#bottom\",\"cullface\":\"down\"},\"up\":{\"uv\":[0,0,16,16],\"texture\":\"#top\"},\"north\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"north\"},\"south\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"south\"},\"west\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"west\"},\"east\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"east\"}}},{\"from\":[7.98,8.02,7.98],\"to\":[16.02,16.02,16.02],\"faces\":{\"up\":{\"uv\":[8,8,16,16],\"texture\":\"#top\",\"cullface\":\"up\"},\"north\":{\"uv\":[0,0,8,8],\"texture\":\"#side\"},\"south\":{\"uv\":[8,0,16,8],\"texture\":\"#side\",\"cullface\":\"south\"},\"west\":{\"uv\":[8,0,16,8],\"texture\":\"#side\"},\"east\":{\"uv\":[0,0,8,8],\"texture\":\"#side\",\"cullface\":\"east\"}}}],\"display\":{\"head\":{\"rotation\":[0,0,0],\"translation\":[0,-6.4,0],\"scale\":[1,1,1]}}}";

    // inventory / hand item model (straight stairs + vanilla stairs display)
    private String stairsItemModelTemplate = "{\"parent\": \"block/block\",\"textures\":{\"particle\":\"{path_side}\",\"bottom\":\"{path_bottom}\",\"side\":\"{path_side}\",\"top\":\"{path_top}\"},\"elements\":[{\"from\":[-0.02,-0.02,-0.02],\"to\":[16.02,8.02,16.02],\"faces\":{\"down\":{\"uv\":[0,0,16,16],\"texture\":\"#bottom\",\"cullface\":\"down\"},\"up\":{\"uv\":[0,0,16,16],\"texture\":\"#top\"},\"north\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"north\"},\"south\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"south\"},\"west\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"west\"},\"east\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"east\"}}},{\"from\":[7.98,8.02,-0.02],\"to\":[16.02,16.02,16.02],\"faces\":{\"up\":{\"uv\":[8,0,16,16],\"texture\":\"#top\",\"cullface\":\"up\"},\"north\":{\"uv\":[0,0,8,8],\"texture\":\"#side\",\"cullface\":\"north\"},\"south\":{\"uv\":[8,0,16,8],\"texture\":\"#side\",\"cullface\":\"south\"},\"west\":{\"uv\":[0,0,16,8],\"texture\":\"#side\"},\"east\":{\"uv\":[0,0,16,8],\"texture\":\"#side\",\"cullface\":\"east\"}}}],\"display\":{\"gui\":{\"rotation\":[30,135,0],\"translation\":[0,0,0],\"scale\":[0.625,0.625,0.625]},\"head\":{\"rotation\":[0,-90,0],\"translation\":[0,0,0],\"scale\":[1,1,1]},\"thirdperson_lefthand\":{\"rotation\":[75,-135,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]}}}";

    // display slabs (note_block face layouts: single / up+down+side / six faces). {path_up}/{path_down}/{path_side} or per-cardinal.
    private String slabBottomSideModelTemplate = "{\"textures\":{\"particle\":\"{path_side}\",\"down\":\"{path_down}\",\"side\":\"{path_side}\",\"up\":\"{path_up}\"},\"elements\":[{\"from\":[-0.02,-0.02,-0.02],\"to\":[16.02,8.02,16.02],\"faces\":{\"down\":{\"uv\":[0,0,16,16],\"texture\":\"#down\",\"cullface\":\"down\"},\"up\":{\"uv\":[0,0,16,16],\"texture\":\"#up\"},\"north\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"north\"},\"south\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"south\"},\"west\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"west\"},\"east\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"east\"}}}],\"display\":{\"head\":{\"translation\":[0,-6.4,0]}}}";

    private String slabTopSideModelTemplate = "{\"textures\":{\"particle\":\"{path_side}\",\"down\":\"{path_down}\",\"side\":\"{path_side}\",\"up\":\"{path_up}\"},\"elements\":[{\"from\":[-0.02,7.98,-0.02],\"to\":[16.02,16.02,16.02],\"faces\":{\"down\":{\"uv\":[0,0,16,16],\"texture\":\"#down\"},\"up\":{\"uv\":[0,0,16,16],\"texture\":\"#up\",\"cullface\":\"up\"},\"north\":{\"uv\":[0,0,16,8],\"texture\":\"#side\",\"cullface\":\"north\"},\"south\":{\"uv\":[0,0,16,8],\"texture\":\"#side\",\"cullface\":\"south\"},\"west\":{\"uv\":[0,0,16,8],\"texture\":\"#side\",\"cullface\":\"west\"},\"east\":{\"uv\":[0,0,16,8],\"texture\":\"#side\",\"cullface\":\"east\"}}}],\"display\":{\"head\":{\"translation\":[0,-6.4,0]}}}";

    private String slabItemSideModelTemplate = "{\"parent\":\"block/block\",\"textures\":{\"particle\":\"{path_side}\",\"down\":\"{path_down}\",\"side\":\"{path_side}\",\"up\":\"{path_up}\"},\"elements\":[{\"from\":[0,0,0],\"to\":[16,8,16],\"faces\":{\"down\":{\"uv\":[0,0,16,16],\"texture\":\"#down\",\"cullface\":\"down\"},\"up\":{\"uv\":[0,0,16,16],\"texture\":\"#up\"},\"north\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"north\"},\"south\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"south\"},\"west\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"west\"},\"east\":{\"uv\":[0,8,16,16],\"texture\":\"#side\",\"cullface\":\"east\"}}}],\"display\":{\"gui\":{\"rotation\":[30,225,0],\"scale\":[0.625,0.625,0.625]},\"ground\":{\"translation\":[0,3,0],\"scale\":[0.25,0.25,0.25]},\"fixed\":{\"scale\":[0.5,0.5,0.5]},\"thirdperson_righthand\":{\"rotation\":[75,45,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]},\"firstperson_righthand\":{\"rotation\":[0,45,0],\"scale\":[0.4,0.4,0.4]},\"firstperson_lefthand\":{\"rotation\":[0,225,0],\"scale\":[0.4,0.4,0.4]}}}";

    private String slabBottomCubeModelTemplate = "{\"textures\":{\"particle\":\"{path_up}\",\"down\":\"{path_down}\",\"up\":\"{path_up}\",\"north\":\"{path_north}\",\"south\":\"{path_south}\",\"east\":\"{path_east}\",\"west\":\"{path_west}\"},\"elements\":[{\"from\":[-0.02,-0.02,-0.02],\"to\":[16.02,8.02,16.02],\"faces\":{\"down\":{\"uv\":[0,0,16,16],\"texture\":\"#down\",\"cullface\":\"down\"},\"up\":{\"uv\":[0,0,16,16],\"texture\":\"#up\"},\"north\":{\"uv\":[0,8,16,16],\"texture\":\"#north\",\"cullface\":\"north\"},\"south\":{\"uv\":[0,8,16,16],\"texture\":\"#south\",\"cullface\":\"south\"},\"west\":{\"uv\":[0,8,16,16],\"texture\":\"#west\",\"cullface\":\"west\"},\"east\":{\"uv\":[0,8,16,16],\"texture\":\"#east\",\"cullface\":\"east\"}}}],\"display\":{\"head\":{\"translation\":[0,-6.4,0]}}}";

    private String slabTopCubeModelTemplate = "{\"textures\":{\"particle\":\"{path_up}\",\"down\":\"{path_down}\",\"up\":\"{path_up}\",\"north\":\"{path_north}\",\"south\":\"{path_south}\",\"east\":\"{path_east}\",\"west\":\"{path_west}\"},\"elements\":[{\"from\":[-0.02,7.98,-0.02],\"to\":[16.02,16.02,16.02],\"faces\":{\"down\":{\"uv\":[0,0,16,16],\"texture\":\"#down\"},\"up\":{\"uv\":[0,0,16,16],\"texture\":\"#up\",\"cullface\":\"up\"},\"north\":{\"uv\":[0,0,16,8],\"texture\":\"#north\",\"cullface\":\"north\"},\"south\":{\"uv\":[0,0,16,8],\"texture\":\"#south\",\"cullface\":\"south\"},\"west\":{\"uv\":[0,0,16,8],\"texture\":\"#west\",\"cullface\":\"west\"},\"east\":{\"uv\":[0,0,16,8],\"texture\":\"#east\",\"cullface\":\"east\"}}}],\"display\":{\"head\":{\"translation\":[0,-6.4,0]}}}";

    private String slabItemCubeModelTemplate = "{\"parent\":\"block/block\",\"textures\":{\"particle\":\"{path_up}\",\"down\":\"{path_down}\",\"up\":\"{path_up}\",\"north\":\"{path_north}\",\"south\":\"{path_south}\",\"east\":\"{path_east}\",\"west\":\"{path_west}\"},\"elements\":[{\"from\":[0,0,0],\"to\":[16,8,16],\"faces\":{\"down\":{\"uv\":[0,0,16,16],\"texture\":\"#down\",\"cullface\":\"down\"},\"up\":{\"uv\":[0,0,16,16],\"texture\":\"#up\"},\"north\":{\"uv\":[0,8,16,16],\"texture\":\"#north\",\"cullface\":\"north\"},\"south\":{\"uv\":[0,8,16,16],\"texture\":\"#south\",\"cullface\":\"south\"},\"west\":{\"uv\":[0,8,16,16],\"texture\":\"#west\",\"cullface\":\"west\"},\"east\":{\"uv\":[0,8,16,16],\"texture\":\"#east\",\"cullface\":\"east\"}}}],\"display\":{\"gui\":{\"rotation\":[30,225,0],\"scale\":[0.625,0.625,0.625]},\"ground\":{\"translation\":[0,3,0],\"scale\":[0.25,0.25,0.25]},\"fixed\":{\"scale\":[0.5,0.5,0.5]},\"thirdperson_righthand\":{\"rotation\":[75,45,0],\"translation\":[0,2.5,0],\"scale\":[0.375,0.375,0.375]},\"firstperson_righthand\":{\"rotation\":[0,45,0],\"scale\":[0.4,0.4,0.4]},\"firstperson_lefthand\":{\"rotation\":[0,225,0],\"scale\":[0.4,0.4,0.4]}}}";

    private String regularItemTemplate = "{\"model\": {\"type\": \"model\", \"model\": \"{path}\"}}\n";

    private List<FileConfiguration> configs = new ArrayList<>();

    private List<FontImageConfig> fontImages = new ArrayList<>();

    /**
     * When true, all font textures are merged into {@code minecraft:default} and {@code minecraft:uniform}.
     * When false (default), first-level folders under {@code textures/font/} are font names.
     */
    private boolean fontsLegacyMode = false;

    private Map<String, FontSettings> fontSettings = new HashMap<>();

    private List<ModelTemplateConfig> modelTemplates = new ArrayList<>();


    private List<ItemTemplateConfig> itemTemplates = new ArrayList<>();


    private List<SoundConfig> sounds = new ArrayList<>();

    record FontImageConfig(int height, int ascent, String path) {


        public BitmapFontImage toFontImage() {
            return new BitmapFontImage(height, ascent, path + ".png");
        }
    }

    /**
     * Per-font build options under {@code fonts.settings.<name>}.
     *
     * @param mergeInto              additional font ids to inject providers into (e.g. {@code minecraft:default})
     * @param appendNegativeSpaces   whether to include the space provider in this font
     */
    public record FontSettings(List<String> mergeInto, boolean appendNegativeSpaces) {

        public static final FontSettings DEFAULT_FONT = new FontSettings(
                List.of("minecraft:default", "minecraft:uniform"), true);

        public static final FontSettings CUSTOM_FONT = new FontSettings(List.of(), true);
    }

    record SoundConfig(String subtitle, double volume, double pith, double weight,
                       boolean stream, double attenuationDistance, boolean preload, String type, List<String> paths) {

        public SoundEvent toSoundEvent(String path) {
            return new SoundEvent(false, subtitle, Lists.newArrayList(
                    new Sound(path.replace("/", "."), volume, pith, weight, stream, attenuationDistance, preload, type)));
        }

        public boolean contains(String path){
            return this.paths.stream().anyMatch(i -> i.replace(".", "/").equalsIgnoreCase(path));
        }
    }

    record ItemTemplateConfig(String prefix, String model) {

    }

    record ModelTemplateConfig(String prefix, String model) {

    }

    public NamespacedConfig(String namespace, File configsDir, File namespaceDir) throws IOException {
        this.namespace = namespace;
        if (!configsDir.exists() || !configsDir.isDirectory())
            return;

        configs.clear();

        for (var file : configsDir.listFiles())
            if (file.getName().endsWith(".yml")) {
                var config = YamlConfiguration.loadConfiguration(file);

                loadModelTemplates(config, namespaceDir);
                loadItemTemplates(config, namespaceDir);
                loadFontImages(config);
                loadFonts(config);
                loadSounds(config);
                configs.add(config);
            }
    }

    private void loadModelTemplates(FileConfiguration config, File namespaceDir) throws IOException {
        var section = config.getConfigurationSection("model-templates");
        if (section == null)
            return;
        for (var i : section.getKeys(false)) {
            var path = "model-templates." + i;

            var templatePath = "model_templates" + "/" + config.getString(path + ".template");

            var template = new File(namespaceDir, templatePath);

            if (!template.exists()) {
                throw new RuntimeException("Provided in " + path + ".template ('" + templatePath + "') template does not exist.");
            }

            this.modelTemplates.add(new ModelTemplateConfig(
                    config.getString(path + ".path"),
                    FileUtils.fileRead(template)
            ));
        }
    }

    private void loadItemTemplates(FileConfiguration config, File namespaceDir) throws IOException {
        var section = config.getConfigurationSection("item-templates");
        if (section == null)
            return;
        for (var i : section.getKeys(false)) {
            var path = "item-templates." + i;

            var templatePath = "item_templates" + "/" + config.getString(path + ".template");

            var template = new File(namespaceDir, templatePath);

            if (!template.exists()) {
                throw new RuntimeException("Provided in " + path + ".template ('" + templatePath + "') template does not exist.");
            }

            this.itemTemplates.add(new ItemTemplateConfig(
                    config.getString(path + ".path"),
                    FileUtils.fileRead(template)
            ));
        }
    }

    private void loadFontImages(FileConfiguration config) {
        var section = config.getConfigurationSection("font-images");
        if (section == null)
            return;
        for (var i : section.getKeys(false)) {
            var path = "font-images." + i;
            this.fontImages.add(new FontImageConfig(
                    config.getInt(path + ".height", 9),
                    config.getInt(path + ".ascent", 8),
                    config.getString(path + ".path")));
        }
    }

    private void loadFonts(FileConfiguration config) {
        var section = config.getConfigurationSection("fonts");
        if (section == null)
            return;

        this.fontsLegacyMode = section.getBoolean("legacy-mode", false);

        var settings = section.getConfigurationSection("settings");
        if (settings == null)
            return;

        for (var fontName : settings.getKeys(false)) {
            var path = "fonts.settings." + fontName;
            List<String> mergeInto;
            if (config.contains(path + ".merge-into")) {
                mergeInto = List.copyOf(config.getStringList(path + ".merge-into"));
            } else if (fontName.equals("default")) {
                mergeInto = FontSettings.DEFAULT_FONT.mergeInto();
            } else {
                mergeInto = FontSettings.CUSTOM_FONT.mergeInto();
            }
            this.fontSettings.put(fontName, new FontSettings(
                    mergeInto,
                    config.getBoolean(path + ".append-negative-spaces", true)));
        }
    }

    public boolean isFontsLegacyMode() {
        return fontsLegacyMode;
    }

    public FontSettings getFontSettings(String fontName) {
        var configured = this.fontSettings.get(fontName);
        if (configured != null)
            return configured;
        return fontName.equals("default") ? FontSettings.DEFAULT_FONT : FontSettings.CUSTOM_FONT;
    }

    private void loadSounds(FileConfiguration config) throws IOException {
        var section = config.getConfigurationSection("sounds");
        if (section == null)
            return;
        for (var i : section.getKeys(false)) {
            var path = "sounds." + i;
            this.sounds.add(new SoundConfig(
                    config.getString(path + ".subtitle"),
                    config.getDouble(path + ".volume", 1.0),
                    config.getDouble(path + ".pith", 1.0),
                    config.getDouble(path + ".weight", 1.0),
                    config.getBoolean(path + ".stream"),
                    config.getDouble(path + ".attenuation-distance"),
                    config.getBoolean(path + ".preload"),
                    config.getString(path + ".type", "file"),
                    config.contains(path + ".paths") ? config.getStringList(path + ".paths") :
                            Lists.newArrayList(config.getString(path + ".path"))
            ));
        }
    }

    public BitmapFontImage getFontImageData(String path) {
        var configFontImage = this.fontImages.stream()
                .filter(i -> i.path().equalsIgnoreCase(path)).findFirst().orElse(null);

        if (configFontImage == null)
            return null;

        return configFontImage.toFontImage();
    }

    public String getModel(String path) {
        return this.modelTemplates.stream().filter(i -> path.startsWith(i.prefix())).findFirst()
                .map(ModelTemplateConfig::model).orElse(null);
    }

    public String getItemModel(String path) {
        path = path.substring(path.indexOf(":") + 1);
        String finalPath = path;
        return this.itemTemplates.stream().filter(i -> finalPath.startsWith(i.prefix())).findFirst()
                .map(ItemTemplateConfig::model).orElse(null);
    }

    public SoundEvent getSound(String path){
        return this.sounds.stream().filter(i -> i.contains(path)).findFirst().map(i -> i.toSoundEvent(path))
                .orElse(null);
    }

    public boolean isAllowAutogen() {
        return allowAutogen;
    }

    public List<String> getAutogenIgnoreList() {
        return autogenIgnoreList;
    }

    public String getGeneratedModelTemplate() {
        return generatedModelTemplate;
    }

    public String getHandheldModelTemplate() {
        return handheldModelTemplate;
    }

    public String getBowModelTemplate() {
        return bowModelTemplate;
    }

    public String getCubeModelTemplate() {
        return cubeModelTemplate;
    }

    public String getCubeSideModelTemplate() {
        return cubeSideModelTemplate;
    }

    public String getCubeAllModelTemplate() {
        return cubeAllModelTemplate;
    }

    public String getStairsStraightModelTemplate() {
        return stairsStraightModelTemplate;
    }

    public String getStairsInnerModelTemplate() {
        return stairsInnerModelTemplate;
    }

    public String getStairsOuterModelTemplate() {
        return stairsOuterModelTemplate;
    }

    public String getStairsItemModelTemplate() {
        return stairsItemModelTemplate;
    }

    public String getStairsModelTemplate(String shape) {
        return switch (shape) {
            case "straight" -> stairsStraightModelTemplate;
            case "inner" -> stairsInnerModelTemplate;
            case "outer" -> stairsOuterModelTemplate;
            default -> throw new IllegalArgumentException("Unknown stairs shape template: " + shape);
        };
    }

    public String getSlabItemModelTemplate(TextureLayout layout) {
        return layout.isSixFace() ? slabItemCubeModelTemplate : slabItemSideModelTemplate;
    }

    public String getSlabModelTemplate(String type, TextureLayout layout) {
        return switch (type) {
            case "bottom" -> layout.isSixFace() ? slabBottomCubeModelTemplate : slabBottomSideModelTemplate;
            case "top" -> layout.isSixFace() ? slabTopCubeModelTemplate : slabTopSideModelTemplate;
            default -> throw new IllegalArgumentException("Unknown slab type template: " + type);
        };
    }

    public String getRegularItemTemplate() {
        return regularItemTemplate;
    }

    public List<String> getExtensionsIgnoreList() {
        return extensionsIgnoreList;
    }

    public List<String> getDirectoriesIgnoreList() {
        return directoriesIgnoreList;
    }
}

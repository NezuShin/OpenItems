package su.nezushin.openitems.rp;

import com.google.common.collect.Lists;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.codehaus.plexus.util.FileUtils;
import su.nezushin.openitems.rp.font.BitmapFontImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private String regularItemTemplate = "{\"model\": {\"type\": \"model\", \"model\": \"{path}\"}}\n";

    private List<FileConfiguration> configs = new ArrayList<>();

    private List<FontImageConfig> fontImages = new ArrayList<>();


    private List<ModelTemplateConfig> modelTemplates = new ArrayList<>();


    private List<ItemTemplateConfig> itemTemplates = new ArrayList<>();

    record FontImageConfig(int height, int ascent, String path) {


        public BitmapFontImage toFontImage() {
            return new BitmapFontImage(height, ascent, path + ".png");
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


    public BitmapFontImage getFontImageData(String path) {
        var configFontImage = this.fontImages.stream()
                .filter(i -> i.path().equalsIgnoreCase(path)).findFirst().orElse(null);

        if (configFontImage == null)
            return null;

        return configFontImage.toFontImage();
    }

    public String getModel(String path) {
        var model = this.modelTemplates.stream().filter(i -> path.startsWith(i.prefix())).findFirst().orElse(null);
        return model == null ? null : model.model();
    }

    public String getItemModel(String path) {
        path = path.substring(path.indexOf(":")+ 1);
        String finalPath = path;
        var model = this.itemTemplates.stream().filter(i -> finalPath.startsWith(i.prefix())).findFirst().orElse(null);
        return model == null ? null : model.model();
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

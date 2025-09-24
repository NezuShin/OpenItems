package su.nezushin.openitems.rp;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class NamespacedConfig {

    private boolean allowAutogen = true;

    private List<String> autogenIgnoreList = new ArrayList<>();


    private List<String> extensionsIgnoreList = Lists.newArrayList(".yml");
    private List<String> directoriesIgnoreList = Lists.newArrayList();


    private String generatedModelTemplate = "{\"parent\":\"minecraft:item/generated\",\"textures\":{\"layer0\":\"{path}\"}}";
    private String handheldModelTemplate = "{\"parent\":\"minecraft:item/handheld\",\"textures\":{\"layer0\":\"{path}\"}}";
    private String bowModelTemplate = "{\"parent\":\"minecraft:item/bow\",\"textures\":{\"layer0\":\"{path}\"}";


    private String regularItemTemplate = "{\"model\": {\"type\": \"model\", \"model\": \"{path}\"}}\n";

    public NamespacedConfig() {

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

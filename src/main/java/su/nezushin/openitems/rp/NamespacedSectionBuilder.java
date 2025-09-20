package su.nezushin.openitems.rp;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.codehaus.plexus.util.FileUtils;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//
public class NamespacedSectionBuilder {

    private String namespace;

    private NamespacedConfig config;

    private File sectionDir;
    private File outputDir;


    public NamespacedSectionBuilder(String namespace, File sectionDir) {
        this.namespace = namespace;

        this.config = new NamespacedConfig();

        this.sectionDir = sectionDir;
        this.outputDir = new File(OpenItems.getInstance().getDataFolder(), "build/assets/" + this.namespace);
    }

    public void build() throws IOException {
        this.outputDir.mkdirs();


        //for items with parent minecraft:generated
        List<ResourcePackScanFile> pngFilesGenerated = new ArrayList<>();

        //for items with parent minecraft:handheld
        List<ResourcePackScanFile> pngFilesHandheld = new ArrayList<>();

        //for textures with own model
        List<ResourcePackScanFile> pngFilesOther = new ArrayList<>();

        //.png.mcmeta and other
        List<ResourcePackScanFile> otherTypeFiles = new ArrayList<>();


        var generatedDir = new File(this.sectionDir, "textures/item/generated");
        var handheldDir = new File(this.sectionDir, "textures/item/handheld");

        if (this.config.isAllowAutogen()) {
            scanForTextures(generatedDir, "item", pngFilesGenerated, otherTypeFiles, new ArrayList<>());
            scanForTextures(handheldDir, "item", pngFilesHandheld, otherTypeFiles, new ArrayList<>());
        }

        scanForTextures(new File(this.sectionDir, "textures"), "", pngFilesHandheld, otherTypeFiles,
                Lists.newArrayList(generatedDir, handheldDir));

        for (var i : pngFilesGenerated)
            createModelAndCopy(i, this.config.getGeneratedModelTemplate());

        for (var i : pngFilesHandheld)
            createModelAndCopy(i, this.config.getHandheldModelTemplate());

        for (var i : Utils.concatLists(pngFilesGenerated, pngFilesHandheld, pngFilesOther, otherTypeFiles)) {
            var dir = new File(this.outputDir, "textures/" + i.path());
            dir.mkdirs();
            FileUtils.copyFile(i.file(), new File(dir + "/" + i.file().getName()));
        }

        scanForModels(new File(this.sectionDir, "models/item"), "");


    }

    record ResourcePackScanFile(File file, String path, String name) {

    }

    private String createPath(String path, File file) {
        return path + (path.isEmpty() ? "" : "/") + file.getName();
    }

    private String getFileName(File file) {
        return file.getName().substring(0, file.getName().lastIndexOf("."));
    }

    public void scanForModels(File file, String path) throws IOException {
        if (!file.isDirectory()) {
            createRegularTemplateItem(path, path, getFileName(file));


            return;
        }
        path = createPath(path, file);
        for (File i : file.listFiles())
            scanForModels(i, path);
    }

    public void scanForTextures(File file, String path, List<ResourcePackScanFile> pngFiles, List<ResourcePackScanFile>
            otherTypeFiles, List<File> ignoreDirs) {
        if (!file.isDirectory()) {
            if (file.getName().toLowerCase().endsWith(".png")) {
                pngFiles.add(new ResourcePackScanFile(file, path, getFileName(file)));
            } else otherTypeFiles.add(new ResourcePackScanFile(file, path, ""));
            return;
        }
        if (ignoreDirs.contains(file)) return;

        path = createPath(path, file);
        for (File i : file.listFiles())
            scanForTextures(i, path, pngFiles, otherTypeFiles, ignoreDirs);

    }

    private void createModelAndCopy(ResourcePackScanFile scanFile, String template) throws IOException {

        var modelPath = this.namespace + ":" + scanFile.path() + "/" + scanFile.name();

        var modelStr = template.replace("{path}", modelPath);

        var modelDir = new File(this.outputDir, "models/" + scanFile.path());

        modelDir.mkdirs();

        Files.write(modelStr.getBytes(StandardCharsets.UTF_8), new File(modelDir, scanFile.name() + ".json"));
        //.replaceFirst("item/", "")

        createRegularTemplateItem(modelPath, scanFile.path().replaceFirst("item/", ""), scanFile.name());
    }

    private void createRegularTemplateItem(String modelPath, String itemPath, String itemName) throws IOException {
        var itemDir = new File(this.outputDir, "items/" + itemPath);

        itemDir.mkdirs();
        Files.write(this.config.getRegularItemTemplate().replace("{path}", modelPath)
                .getBytes(StandardCharsets.UTF_8), new File(itemDir, itemName + ".json"));
    }
}

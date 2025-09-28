package su.nezushin.openitems.rp;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        //for blocks with parent minecraft:cube_all
        List<ResourcePackScanFile> pngFilesNoteblock = new ArrayList<>();

        var generatedDir = new File(this.sectionDir, "textures/item/generated");
        var handheldDir = new File(this.sectionDir, "textures/item/handheld");

        var noteblockDir = new File(this.sectionDir, "textures/block/noteblock");

        if (this.config.isAllowAutogen()) {
            scanForItemTextures(generatedDir, "item", pngFilesGenerated, new ArrayList<>());
            scanForItemTextures(handheldDir, "item", pngFilesHandheld, new ArrayList<>());
            scanForItemTextures(noteblockDir, "block", pngFilesNoteblock, new ArrayList<>());
        }

        Utils.copyFolder(this.sectionDir, outputDir, this.sectionDir, this.config.getDirectoriesIgnoreList(), this.config.getExtensionsIgnoreList());

        for (var i : pngFilesGenerated)
            createItemModel(i, this.config.getGeneratedModelTemplate());

        for (var i : pngFilesHandheld)
            createItemModel(i, this.config.getHandheldModelTemplate());


        var tripwireDir = new File(this.sectionDir, "models/block/tripwire");

        prepareModels(pngFilesNoteblock);
        scanForTripwireModels(tripwireDir, "block");

        scanForItemModels(new File(this.sectionDir, "models/item"), "");


    }

    record ResourcePackScanFile(File file, String path, String name) {

        public String pathAndName() {
            return path + "/" + name;
        }

    }

    //scan for item models to add it into /items dir
    public void scanForItemModels(File file, String path) throws IOException {
        if (!file.isDirectory()) {
            createRegularTemplateItem(this.namespace + ":" + path + "/" + Utils.getFileName(file), path.replaceFirst("item", ""), Utils.getFileName(file));
            return;
        }
        path = Utils.createPath(path, file);
        for (File i : file.listFiles())
            scanForItemModels(i, path);
    }

    //scan for textures to create model and add to /items dir
    public void scanForItemTextures(File file, String path, List<ResourcePackScanFile> pngFiles, List<File> ignoreDirs) {
        if (!file.isDirectory()) {
            if (file.getName().toLowerCase().endsWith(".png")) {
                pngFiles.add(new ResourcePackScanFile(file, path, Utils.getFileName(file)));
            }
            return;
        }
        if (ignoreDirs.contains(file)) return;

        path = Utils.createPath(path, file);
        for (File i : file.listFiles())
            scanForItemTextures(i, path, pngFiles, ignoreDirs);

    }

    //scan for models to add it into block registry and /items dir
    public void scanForTripwireModels(File file, String path) throws IOException {
        if (!file.exists())
            return;
        if (!file.isDirectory()) {
            createRegularTemplateItem(this.namespace + ":" + path + "/" + Utils.getFileName(file),
                    path.replaceFirst("block ", ""), Utils.getFileName(file));

            var modelPath = this.namespace + ":" + path + "/" + Utils.getFileName(file);

            var blockIdCache = OpenItems.getInstance().getResourcePackBuilder().getBlockIdCache();

            var id = blockIdCache.getOrCreateTripwireId(modelPath);
            blockIdCache.getRegistredTripwireIds().put(modelPath, id);
            return;
        }
        path = Utils.createPath(path, file);
        for (File i : file.listFiles())
            scanForTripwireModels(i, path);

    }

    private void prepareModels(List<ResourcePackScanFile> scanFiles) throws IOException {
        /*

        case 1 - cube (each for every side):
        block_id_up.png
        block_id_down.png
        block_id_west.png
        block_id_east.png
        block_id_south.png
        block_id_west.png

        case 2 - cube (one for every side):
        block_id_up.png
        block_id_side.png
        block_id_down.png


        case 3 - cube_all:
        block_id.png
         */
        scanForBlockModels(scanFiles, this.config.getCubeModelTemplate(), Sets.newHashSet("up", "down", "west", "east", "south", "north"));
        scanForBlockModels(scanFiles, this.config.getCubeSideModelTemplate(), Sets.newHashSet("up", "down", "side"));
        for (var i : new ArrayList<>(scanFiles))
            createBlockModel(scanFiles, i.pathAndName(), this.config.getCubeAllModelTemplate(), Sets.newHashSet(""));
    }

    public void scanForBlockModels(List<ResourcePackScanFile> scanFiles, String template, Set<String> faces) throws IOException {
        var blocks = new HashSet<>(scanFiles.stream().filter(i -> {
                    //filter for all textures that have "_" in name and ends with name of any face (e.g. west)
                    if (!i.name().contains("_")) return false;
                    var face = i.name().substring(i.name().lastIndexOf("_") + 1);
                    return faces.contains(face);
                })
                //remove face from texture name
                .map(i -> i.pathAndName().substring(0, i.pathAndName().lastIndexOf("_")))
                .filter(i -> {
                    //ensure there is at least one texture for any face
                    for (var side : faces)
                        if (!scanFiles.stream().anyMatch(j -> j.pathAndName().equalsIgnoreCase(i + "_" + side)))
                            return false;

                    return true;
                }).toList());

        for (var i : blocks)
            createBlockModel(scanFiles, i, template, faces);

    }

    public void createBlockModel(List<ResourcePackScanFile> scanFiles, String pathAndName, String template, Set<String> faces) throws IOException {
        File modelDir = null;
        for (var face : faces) {
            var scanFile = getBlockFaceTexture(scanFiles, pathAndName + (face.isEmpty() ? "" : "_") + face);

            scanFiles.remove(scanFile);

            if (modelDir == null) modelDir = new File(this.outputDir, "models/" + scanFile.path());

            template = template.replace("{path" + (face.isEmpty() ? "" : "_") + face + "}", this.namespace + ":" + scanFile.pathAndName());
        }

        var modelPath = this.namespace + ":" + pathAndName;

        var blockIdCache = OpenItems.getInstance().getResourcePackBuilder().getBlockIdCache();

        var id = blockIdCache.getOrCreateNoteblockId(modelPath);
        blockIdCache.getRegistredNoteblockIds().put(modelPath, id);

        modelDir.mkdirs();

        Files.write(template.getBytes(StandardCharsets.UTF_8), new File(this.outputDir + "/models/" + pathAndName + ".json"));

        createRegularTemplateItem(modelPath, pathAndName.substring(0, pathAndName.lastIndexOf("/")), pathAndName.substring(pathAndName.lastIndexOf("/") + 1));
    }

    private ResourcePackScanFile getBlockFaceTexture(List<ResourcePackScanFile> scanFiles, String name) {
        return scanFiles.stream().filter(i -> i.pathAndName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }


    private void createItemModel(ResourcePackScanFile scanFile, String template) throws IOException {

        var modelPath = this.namespace + ":" + scanFile.path() + "/" + scanFile.name();

        var modelStr = template.replace("{path}", modelPath);

        var modelDir = new File(this.outputDir, "models/" + scanFile.path());

        modelDir.mkdirs();

        Files.write(modelStr.getBytes(StandardCharsets.UTF_8), new File(modelDir, scanFile.name() + ".json"));

        createRegularTemplateItem(modelPath, scanFile.path(), scanFile.name());
    }

    private void createRegularTemplateItem(String modelPath, String itemPath, String itemName) throws IOException {
        var itemDir = new File(this.outputDir, "items/" + itemPath);

        itemDir.mkdirs();
        Files.write(this.config.getRegularItemTemplate().replace("{path}", modelPath).getBytes(StandardCharsets.UTF_8), new File(itemDir, itemName + ".json"));
    }
}

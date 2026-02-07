package su.nezushin.openitems.rp;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.rp.equipment.EquipmentModel;
import su.nezushin.openitems.rp.font.BitmapFontImage;
import su.nezushin.openitems.rp.sound.Sound;
import su.nezushin.openitems.rp.sound.SoundEvent;
import su.nezushin.openitems.utils.OpenItemsConfig;
import su.nezushin.openitems.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Builder and generator for every namespace
 */
public class NamespacedSectionBuilder {

    private String namespace;

    private NamespacedConfig config;

    private File sectionDir;
    private File outputDir;


    public NamespacedSectionBuilder(String namespace, File sectionDir) throws IOException {
        this.namespace = namespace;

        this.config = new NamespacedConfig(namespace, new File(sectionDir, "configs"), sectionDir);

        this.sectionDir = sectionDir;
        this.outputDir = new File(OpenItems.getInstance().getDataFolder(), "build/assets/" + this.namespace);
    }

    public List<File> getAllTextures() {
        List<ResourcePackScanFile> scanFiles = new ArrayList<>();

        scanForTextures(new File(this.sectionDir, "textures"), "", true, scanFiles);

        return scanFiles.stream().map(ResourcePackScanFile::file).toList();
    }

    public void build() throws IOException {
        this.outputDir.mkdirs();


        //for items with parent minecraft:generated
        List<ResourcePackScanFile> pngFilesGenerated = new ArrayList<>();

        //for items with parent minecraft:handheld
        List<ResourcePackScanFile> pngFilesHandheld = new ArrayList<>();

        //for blocks with parent minecraft:cube_all
        List<ResourcePackScanFile> pngFilesNoteblock = new ArrayList<>();


        //for any arbitrary textures with custom model templates
        List<ResourcePackScanFile> pngFilesCustomModelTemplates = new ArrayList<>();


        //for sounds
        List<ResourcePackScanFile> oggFiles = new ArrayList<>();

        //for font images
        List<ResourcePackScanFile> pngFilesEmoji = new ArrayList<>();

        var generatedDir = new File(this.sectionDir, "textures/item/generated");
        var handheldDir = new File(this.sectionDir, "textures/item/handheld");

        var noteblockDir = new File(this.sectionDir, "textures/block/note_block");
        var tripwireDir = new File(this.sectionDir, "models/block/tripwire");
        var chorusDir = new File(this.sectionDir, "models/block/chorus_plant");


        var customModelTemplatesDir = new File(this.sectionDir, "textures/");

        var equipmentDir = new File(this.sectionDir, "textures/entity/equipment");

        var fontDir = new File(this.sectionDir, "textures/font");


        var sounds = new File(this.sectionDir, "sounds");

        if (this.config.isAllowAutogen()) {
            scanForTextures(generatedDir, "item", true, pngFilesGenerated);
            scanForTextures(handheldDir, "item", true, pngFilesHandheld);
            scanForTextures(noteblockDir, "block", true, pngFilesNoteblock);
            scanForTextures(fontDir, "", true, pngFilesEmoji);

            scanForTextures(customModelTemplatesDir, "", false, pngFilesCustomModelTemplates);

            scanForSounds(sounds, "", false, oggFiles);
        }



        for (var i : pngFilesGenerated)
            createItemModel(i, this.config.getGeneratedModelTemplate());

        for (var i : pngFilesHandheld)
            createItemModel(i, this.config.getHandheldModelTemplate());

        for (var i : pngFilesCustomModelTemplates) {
            var model = this.config.getModel(i.pathAndName());
            if (model != null) {
                createItemModel(i, model);
            }
        }


        Map<String, SoundEvent> soundsMap = new HashMap<>();
        for (var i : oggFiles) {
            var sound = this.config.getSound(i.pathAndName());

            if (sound == null)
                sound = new SoundEvent(false, null, Lists.newArrayList(
                        new Sound(this.namespace + ":" + i.pathAndName(), 1.0, 1.0, 1.0,
                                false, 16, false, "file")
                ));
            soundsMap.put(i.pathAndName().replace("/", "."), sound);
        }

        Files.write(OpenItems.getInstance().getGson().toJson(soundsMap).getBytes(StandardCharsets.UTF_8),
                new File(this.outputDir, "sounds.json"));


        var fontImageCache = OpenItems.getInstance().getResourcePackBuilder().getFontImagesIdCache();


        for (var i : pngFilesEmoji) {
            var path = this.namespace + ":" + i.pathAndName();
            var id = fontImageCache.getOrCreateFontImageId(path);
            var data = this.config.getFontImageData(path);
            if (data == null) {
                var map = Utils.extractNumbers(i.name());

                var height = map.getOrDefault("h", 9);
                var ascent = map.getOrDefault("a", 8);


                data = new BitmapFontImage(height, ascent, path + ".png");


                path = this.namespace + ":" + Utils.createPath(i.path(), i.name()
                        .replace("_h" + height, "")
                        .replace("h" + height, "")
                        .replace("_a" + ascent, "")
                        .replace("a" + ascent, ""));
            }
            data.setSymbol(String.valueOf((char) id));
            fontImageCache.getRegisteredCharIds().put(path, data);
        }

        generateEquipmentModels(equipmentDir);

        generateNoteblockModels(pngFilesNoteblock);
        scanForTripwireModels(tripwireDir, "block");
        scanForChorusModels(chorusDir, "block");

        scanForItemModels(new File(this.sectionDir, "models/item"), "");




        Utils.copyFolder(this.sectionDir, outputDir, this.sectionDir, this.config.getDirectoriesIgnoreList(), this.config.getExtensionsIgnoreList());
    }

    private record ResourcePackScanFile(File file, String path, String name) {

        public String pathAndName() {
            return Utils.createPath(path, name);
        }

    }

    //find all equipment textures
    public void scanForEquipment(File file, String layer, String path, boolean applyPath,
                                 Map<String, List<String>> layerNameMap) {
        if (!file.exists())
            return;
        if (!file.isDirectory()) {
            var mapPath = Utils.createPath(path, Utils.getFileName(file));
            var list = layerNameMap.getOrDefault(mapPath, new ArrayList<>());

            //map.put(layer, new ResourcePackScanFile(file, path, Utils.getFileName(file)));
            list.add(layer);

            layerNameMap.put(mapPath, list);
            return;
        }

        if (applyPath)

            path = Utils.createPath(path, file);
        for (File i : file.listFiles()) {
            scanForEquipment(i, layer, path, true, layerNameMap);
        }
    }


    //scan for sounds
    public void scanForSounds(File file, String path, boolean applyPath, List<ResourcePackScanFile> soundFiles) throws IOException {
        if (!file.exists())
            return;
        if (!file.isDirectory()) {
            if (file.getName().toLowerCase().endsWith(".ogg")) {
                soundFiles.add(new ResourcePackScanFile(file, path, Utils.getFileName(file)));
            }
            return;
        }
        if (applyPath)
            path = Utils.createPath(path, file);
        for (File i : file.listFiles())
            scanForSounds(i, path, true, soundFiles);
    }

    //scan for item models to add it into /items dir
    public void scanForItemModels(File file, String path) throws IOException {
        if (!file.exists())
            return;
        if (!file.isDirectory()) {
            createRegularTemplateItem(this.namespace + ":" + path + "/" + Utils.getFileName(file), path, Utils.getFileName(file));
            return;
        }
        path = Utils.createPath(path, file);
        for (File i : file.listFiles())
            scanForItemModels(i, path);
    }

    //scan for textures to create model and add to /items dir
    public void scanForTextures(File file, String path, boolean applyPath, List<ResourcePackScanFile> pngFiles) {
        if (!file.exists())
            return;
        if (!file.isDirectory()) {
            if (file.getName().toLowerCase().endsWith(".png")) {
                pngFiles.add(new ResourcePackScanFile(file, path, Utils.getFileName(file)));
            }
            return;
        }
        if (applyPath)
            path = Utils.createPath(path, file);
        for (File i : file.listFiles())
            scanForTextures(i, path, true, pngFiles);

    }

    //scan for models to add it into block registry and /items dir
    public void scanForTripwireModels(File file, String path) throws IOException {
        if (!OpenItemsConfig.enableTripwires)
            return;
        if (!file.exists())
            return;
        if (!file.isDirectory()) {
            createRegularTemplateItem(this.namespace + ":" + path + "/" + Utils.getFileName(file),
                    path.replaceFirst("block ", ""), Utils.getFileName(file));

            var modelPath = this.namespace + ":" + path + "/" + Utils.getFileName(file);

            var blockIdCache = OpenItems.getInstance().getResourcePackBuilder().getBlockIdCache();

            var id = blockIdCache.getOrCreateTripwireId(modelPath);
            blockIdCache.getRegisteredTripwireIds().put(modelPath, id);
            return;
        }
        path = Utils.createPath(path, file);
        for (File i : file.listFiles())
            scanForTripwireModels(i, path);
    }

    //scan for models to add it into block registry and /items dir
    public void scanForChorusModels(File file, String path) throws IOException {
        if (!OpenItemsConfig.enableChorus)
            return;
        if (!file.exists())
            return;
        if (!file.isDirectory()) {
            createRegularTemplateItem(this.namespace + ":" + path + "/" + Utils.getFileName(file),
                    path.replaceFirst("block ", ""), Utils.getFileName(file));

            var modelPath = this.namespace + ":" + path + "/" + Utils.getFileName(file);

            var blockIdCache = OpenItems.getInstance().getResourcePackBuilder().getBlockIdCache();

            var id = blockIdCache.getOrCreateChorusId(modelPath);
            blockIdCache.getRegisteredChorusIds().put(modelPath, id);
            return;
        }
        path = Utils.createPath(path, file);
        for (File i : file.listFiles())
            scanForChorusModels(i, path);
    }

    private void generateEquipmentModels(File equipmentDir) throws IOException {
        if (!equipmentDir.exists())
            return;
        Map<String, List<String>> layerNameMap = new HashMap<>();
        for (var i : equipmentDir.listFiles()) {
            scanForEquipment(i, i.getName(), "", false, layerNameMap);
        }

        for (var i : layerNameMap.entrySet()) {
            var equipmentModelFile = new File(this.outputDir, "equipment/" + i.getKey() + ".json");
            equipmentModelFile.getParentFile().mkdirs();
            Files.write(OpenItems.getInstance().getGson().toJson(
                    new EquipmentModel(this.namespace + ":" + i.getKey(), i.getValue()))
                            .getBytes(StandardCharsets.UTF_8),
                    equipmentModelFile);
        }
    }

    private void generateNoteblockModels(List<ResourcePackScanFile> scanFiles) throws IOException {
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
        blockIdCache.getRegisteredNoteblockIds().put(modelPath, id);

        modelDir.mkdirs();

        Files.write(template.getBytes(StandardCharsets.UTF_8), new File(this.outputDir + "/models/" + pathAndName + ".json"));

        createRegularTemplateItem(modelPath, pathAndName.substring(0, pathAndName.lastIndexOf("/")), pathAndName.substring(pathAndName.lastIndexOf("/") + 1));
    }

    private ResourcePackScanFile getBlockFaceTexture(List<ResourcePackScanFile> scanFiles, String name) {
        return scanFiles.stream().filter(i -> i.pathAndName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    //For handheld and generated models
    private void createItemModel(ResourcePackScanFile scanFile, String template) throws IOException {

        var modelPath = this.namespace + ":" + scanFile.path() + "/" + scanFile.name();

        var modelStr = template.replace("{path}", modelPath);

        var modelDir = new File(this.outputDir, "models/" + scanFile.path());

        modelDir.mkdirs();

        Files.write(modelStr.getBytes(StandardCharsets.UTF_8), new File(modelDir, scanFile.name() + ".json"));


        createRegularTemplateItem(modelPath, scanFile.path(), scanFile.name());
    }

    //Add link to model in /items dir
    private void createRegularTemplateItem(String modelPath, String itemPath, String itemName) throws IOException {
        var itemDir = new File(this.outputDir, "items/" + itemPath);

        itemDir.mkdirs();
        var template = this.config.getItemModel(modelPath);

        if (template == null || template.isEmpty())
            template = this.config.getRegularItemTemplate();

        Files.write(template.replace("{path}", modelPath).getBytes(StandardCharsets.UTF_8), new File(itemDir, itemName + ".json"));
    }
}

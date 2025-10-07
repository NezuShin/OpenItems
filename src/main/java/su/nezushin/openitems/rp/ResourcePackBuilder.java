package su.nezushin.openitems.rp;

import org.bukkit.Bukkit;
import org.codehaus.plexus.util.FileUtils;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.types.CustomChorusModel;
import su.nezushin.openitems.blocks.types.CustomTripwireModel;
import su.nezushin.openitems.events.AsyncBuildDoneEvent;
import su.nezushin.openitems.events.AsyncRegistryLoadedEvent;
import su.nezushin.openitems.rp.cache.BlockIdCache;
import su.nezushin.openitems.rp.cache.FontImageIdCache;
import su.nezushin.openitems.utils.Message;
import su.nezushin.openitems.utils.OpenItemsConfig;
import su.nezushin.openitems.utils.Utils;
import su.nezushin.openitems.blocks.types.CustomNoteblockModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Main builder. Used to build resource pack and load registry
 */
public class ResourcePackBuilder {

    private BlockIdCache blockIdCache;
    private FontImageIdCache fontImageIdCache;
    private boolean hasMipMapProblem = false;

    public ResourcePackBuilder() {
        try {
            this.loadCache();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void loadRegistry() {
        try {
            loadCache();
            fillRegistry();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void loadCache() throws IOException {
        this.blockIdCache = new BlockIdCache();
        this.blockIdCache.load();

        this.fontImageIdCache = new FontImageIdCache();
        this.fontImageIdCache.load();
    }

    public List<File> getAllTextures() {
        var list = new ArrayList<File>();

        for (var i : getContentNamespaces())
            list.addAll(new NamespacedSectionBuilder(i.getName(), i).getAllTextures());

        return list;
    }

    public File getContentsDirectory() {
        return new File(OpenItems.getInstance().getDataFolder(), "contents");
    }

    private List<File> getContentNamespaces() {
        var contents = getContentsDirectory();

        contents.mkdirs();

        return Arrays.stream(Objects.requireNonNull(contents.listFiles())).filter(File::isDirectory).toList();
    }

    public boolean build() {
        try {
            var startTime = System.currentTimeMillis();
            OpenItems.getInstance().getModelRegistry().setLock(false);
            this.loadCache();
            this.clean();


            for (var i : getContentNamespaces()) new NamespacedSectionBuilder(i.getName(), i).build();

            this.blockIdCache.build();
            this.blockIdCache.save();
            this.fontImageIdCache.build();
            this.fontImageIdCache.save();

            fillRegistry();


            for (var out : OpenItemsConfig.getResourcePackCopyDestinationFiles()) {
                FileUtils.deleteDirectory(out);
                var build = new File(OpenItems.getInstance().getDataFolder(), "build");
                Utils.copyFolder(build, out, build, new ArrayList<>(), new ArrayList<>());
            }

            if (!OpenItemsConfig.disableMipMapWarning)
                hasMipMapProblem = !Utils.checkMipMap().isEmpty();

            if (hasMipMapProblem) {
                Message.build_mip_map_warning.send(Bukkit.getConsoleSender());
            }

            OpenItems.async(() -> Bukkit.getPluginManager().callEvent(new AsyncBuildDoneEvent()));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void fillRegistry() throws IOException {
        try {
            OpenItems.getInstance().getModelRegistry().setLock(false);
            var contents = new File(OpenItems.getInstance().getDataFolder(), "build/assets");

            for (var namespace : contents.listFiles()) {
                if (namespace.isDirectory()) {
                    var items = new File(namespace, "items");
                    if (items.exists() && items.isDirectory()) scanForItems(items, namespace.getName(), "",
                            false,
                            (str) -> OpenItems.getInstance().getModelRegistry().getItems().add(str));

                    var equipment = new File(namespace, "equipment");
                    if (items.exists() && items.isDirectory()) scanForItems(equipment, namespace.getName(), "",
                            false,
                            (str) -> OpenItems.getInstance().getModelRegistry().getEquipment().add(str));

                }
            }


            this.blockIdCache.getRegisteredNoteblockIds().forEach((k, v) -> {
                OpenItems.getInstance().getModelRegistry().getBlockTypes().put(k, new CustomNoteblockModel(v));
            });
            this.blockIdCache.getRegisteredTripwireIds().forEach((k, v) -> {
                OpenItems.getInstance().getModelRegistry().getBlockTypes().put(k, new CustomTripwireModel(v));
            });
            this.blockIdCache.getRegisteredChorusIds().forEach((k, v) -> {
                OpenItems.getInstance().getModelRegistry().getBlockTypes().put(k, new CustomChorusModel(v));
            });

            this.fontImageIdCache.getRegisteredCharIds().forEach((k, v) -> {
                OpenItems.getInstance().getModelRegistry().getFontImages().put(k, v.getSymbol());
            });

            OpenItems.async(() -> Bukkit.getPluginManager().callEvent(new AsyncRegistryLoadedEvent()));
        } catch (Exception ex) {
            throw ex;
        } finally {
            OpenItems.getInstance().getModelRegistry().setLock(true);
        }
    }

    private interface Callback<T> {
        public void run(T t);
    }

    private void scanForItems(File file, String namespace, String path, boolean appendPath, Callback<String> callback) throws IOException {
        if (!file.isDirectory()) {

            callback.run(namespace + ":" + Utils.createPath(path, Utils.getFileName(file)));
            return;
        }
        if (appendPath) path = Utils.createPath(path, file);
        for (File i : file.listFiles())
            scanForItems(i, namespace, path, true, callback);
    }

    public void clean() throws IOException {
        this.blockIdCache.cleanRegistered();
        this.fontImageIdCache.cleanRegistered();
        OpenItems.getInstance().getModelRegistry().clear();
        var dir = new File(OpenItems.getInstance().getDataFolder(), "build/assets");

        if (dir.exists()) FileUtils.deleteDirectory(dir);


        for (var i : OpenItemsConfig.getResourcePackCopyDestinationFiles())
            FileUtils.deleteDirectory(i);
    }

    public FontImageIdCache getFontImagesIdCache() {
        return fontImageIdCache;
    }

    public BlockIdCache getBlockIdCache() {
        return blockIdCache;
    }

    public boolean isHasMipMapProblem() {
        return hasMipMapProblem;
    }
}

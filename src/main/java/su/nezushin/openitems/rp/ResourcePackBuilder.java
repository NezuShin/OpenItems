package su.nezushin.openitems.rp;

import org.bukkit.Bukkit;
import org.codehaus.plexus.util.FileUtils;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.types.CustomTripwireModel;
import su.nezushin.openitems.events.AsyncOpenItemsBuildDoneEvent;
import su.nezushin.openitems.rp.cache.BlockIdCache;
import su.nezushin.openitems.rp.cache.FontImagesIdCache;
import su.nezushin.openitems.utils.OpenItemsConfig;
import su.nezushin.openitems.utils.Utils;
import su.nezushin.openitems.blocks.types.CustomNoteblockModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Main builder. Used to build resource pack and load registry
 */
public class ResourcePackBuilder {

    private BlockIdCache blockIdCache;
    private FontImagesIdCache fontImagesIdCache;

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

        this.fontImagesIdCache = new FontImagesIdCache();
        this.fontImagesIdCache.load();
    }


    public boolean build() {
        try {
            OpenItems.getInstance().getModelRegistry().setLock(false);
            this.loadCache();
            this.clean();

            var contents = new File(OpenItems.getInstance().getDataFolder(), "contents");

            contents.mkdirs();

            for (var i : contents.listFiles())
                if (i.isDirectory()) new NamespacedSectionBuilder(i.getName(), i).build();

            this.blockIdCache.build();
            this.blockIdCache.save();
            this.fontImagesIdCache.build();
            this.fontImagesIdCache.save();

            fillRegistry();


            for (var out : OpenItemsConfig.getResourcePackCopyDestinationFiles()) {
                FileUtils.deleteDirectory(out);
                var build = new File(OpenItems.getInstance().getDataFolder(), "build");
                Utils.copyFolder(build, out, build, new ArrayList<>(), new ArrayList<>());
            }
            OpenItems.async(() -> Bukkit.getPluginManager().callEvent(new AsyncOpenItemsBuildDoneEvent(true)));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            OpenItems.getInstance().getModelRegistry().setLock(true);
        }
    }

    public void fillRegistry() throws IOException {
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

        this.fontImagesIdCache.getRegisteredCharIds().forEach((k, v) -> {
            OpenItems.getInstance().getModelRegistry().getFontImages().put(k, v.getSymbol());
        });
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
        this.fontImagesIdCache.cleanRegistered();
        OpenItems.getInstance().getModelRegistry().clear();
        var dir = new File(OpenItems.getInstance().getDataFolder(), "build/assets");


        if (dir.exists()) FileUtils.deleteDirectory(dir);

        OpenItemsConfig.getResourcePackCopyDestinationFiles().forEach(i -> i.delete());
    }

    public FontImagesIdCache getFontImagesIdCache() {
        return fontImagesIdCache;
    }

    public BlockIdCache getBlockIdCache() {
        return blockIdCache;
    }
}

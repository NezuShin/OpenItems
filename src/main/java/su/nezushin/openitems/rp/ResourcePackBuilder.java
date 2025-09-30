package su.nezushin.openitems.rp;

import org.codehaus.plexus.util.FileUtils;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.types.CustomTripwireModel;
import su.nezushin.openitems.utils.OpenItemsConfig;
import su.nezushin.openitems.utils.Utils;
import su.nezushin.openitems.blocks.types.CustomNoteblockModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ResourcePackBuilder {

    private BlockIdCache blockIdCache;

    public ResourcePackBuilder() {
        try {
            this.loadCache();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void loadCache() throws IOException {
        this.blockIdCache = new BlockIdCache();
        this.blockIdCache.load();
    }


    public void build() {
        try {
            this.loadCache();
            this.clean();

            var contents = new File(OpenItems.getInstance().getDataFolder(), "contents");

            contents.mkdirs();

            for (var i : contents.listFiles())
                if (i.isDirectory()) new NamespacedSectionBuilder(i.getName(), i).build();

            this.blockIdCache.build();
            this.blockIdCache.save();

            fillRegistry();


            for (var out : OpenItemsConfig.getResourcePackCopyDestinationFiles()) {
                FileUtils.deleteDirectory(out);
                var build = new File(OpenItems.getInstance().getDataFolder(), "build");
                Utils.copyFolder(build, out, build, new ArrayList<>(), new ArrayList<>());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void fillRegistry() throws IOException {
        var contents = new File(OpenItems.getInstance().getDataFolder(), "build/assets");

        for (var namespace : contents.listFiles()) {
            if (namespace.isDirectory()) {
                var items = new File(namespace, "items");
                if (items.exists() && items.isDirectory()) scanForItems(items, namespace.getName(), "", false);
            }
        }

        this.blockIdCache.getRegistredNoteblockIds().forEach((k, v) -> {
            OpenItems.getInstance().getModelRegistry().getBlockTypes().put(k, new CustomNoteblockModel(v));
        });
        this.blockIdCache.getRegistredTripwireIds().forEach((k, v) -> {
            OpenItems.getInstance().getModelRegistry().getBlockTypes().put(k, new CustomTripwireModel(v));
        });
    }

    public void scanForItems(File file, String namespace, String path, boolean appendPath) throws IOException {
        if (!file.isDirectory()) {
            OpenItems.getInstance().getModelRegistry().getItems().add(namespace + ":" + Utils.createPath(path, Utils.getFileName(file)));
            return;
        }
        if (appendPath) path = Utils.createPath(path, file);
        for (File i : file.listFiles())
            scanForItems(i, namespace, path, true);
    }

    public void clean() throws IOException {
        this.blockIdCache.cleanRegistred();
        OpenItems.getInstance().getModelRegistry().clear();
        var dir = new File(OpenItems.getInstance().getDataFolder(), "build/assets");


        if (dir.exists()) FileUtils.deleteDirectory(dir);

        OpenItemsConfig.getResourcePackCopyDestinationFiles().forEach(i -> i.delete());
    }

    public BlockIdCache getBlockIdCache() {
        return blockIdCache;
    }
}

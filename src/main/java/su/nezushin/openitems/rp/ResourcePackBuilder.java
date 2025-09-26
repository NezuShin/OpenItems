package su.nezushin.openitems.rp;

import com.google.common.base.Charsets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.Material;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.Utils;
import su.nezushin.openitems.blocks.BlockStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResourcePackBuilder {

    private BlockIdCache blockIdCache;

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

    public void clean() {
        var dir = new File(OpenItems.getInstance().getDataFolder(), "build/assets");

        if (dir.exists()) dir.delete();
    }

    public BlockIdCache getBlockIdCache() {
        return blockIdCache;
    }
}

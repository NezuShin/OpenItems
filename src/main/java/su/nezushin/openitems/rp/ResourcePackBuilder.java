package su.nezushin.openitems.rp;

import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.Utils;

import java.io.File;
import java.io.IOException;

public class ResourcePackBuilder {

    public void build() {
        try {
            this.clean();

            var contents = new File(OpenItems.getInstance().getDataFolder(), "contents");

            contents.mkdirs();

            for (var i : contents.listFiles())
                if (i.isDirectory()) new NamespacedSectionBuilder(i.getName(), i).build();


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


}

package su.nezushin.openitems.rp;

import su.nezushin.openitems.OpenItems;

import java.io.File;

public class ResourcePackBuilder {

    public void build() {
        try {
            this.clean();

            var contents = new File(OpenItems.getInstance().getDataFolder(), "contents");

            contents.mkdirs();

            for (var i : contents.listFiles())
                if (i.isDirectory())
                    new NamespacedSectionBuilder(i.getName(), i).build();


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void clean() {
        var dir = new File(OpenItems.getInstance().getDataFolder(), "build/assets");

        if (dir.exists())
            dir.delete();
    }


}

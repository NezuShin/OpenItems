package su.nezushin.openitems.rp.cache;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import su.nezushin.openitems.OpenItems;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract class JsonCache {


    protected abstract String getName();

    public void save() throws IOException {
        var cacheFile = new File(OpenItems.getInstance().getDataFolder(), getName() + ".json");

        Files.writeString(cacheFile.toPath(), new Gson().toJson(this), Charsets.UTF_8);
    }

    public void load() throws IOException {
        var cacheFile = new File(OpenItems.getInstance().getDataFolder(), getName() + ".json");

        if (!cacheFile.exists())
            return;

        new GsonBuilder()
                .registerTypeAdapter(getClass(), (InstanceCreator<?>) type ->  this)
                .create()
                .fromJson(Files.readString(cacheFile.toPath(), Charsets.UTF_8), getClass());
    }
}

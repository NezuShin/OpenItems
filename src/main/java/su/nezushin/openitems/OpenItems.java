package su.nezushin.openitems;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import su.nezushin.openitems.rp.ResourcePackBuilder;

public final class OpenItems extends JavaPlugin {

    private static OpenItems instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        new ResourcePackBuilder().build();
        Bukkit.getPluginManager().registerEvents(new TestListener(), instance);
    }

    @Override
    public void onDisable() {
    }

    public static OpenItems getInstance() {
        return instance;
    }
}

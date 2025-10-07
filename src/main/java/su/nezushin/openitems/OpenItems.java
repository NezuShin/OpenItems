package su.nezushin.openitems;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import su.nezushin.openitems.armor.ArmorDamagePreventListener;
import su.nezushin.openitems.blocks.CustomBlocks;
import su.nezushin.openitems.cmd.OEditCommand;
import su.nezushin.openitems.cmd.OItemsCommand;
import su.nezushin.openitems.rp.ResourcePackBuilder;
import su.nezushin.openitems.utils.OpenItemsConfig;
import su.nezushin.openitems.utils.Utils;

public final class OpenItems extends JavaPlugin {

    private static OpenItems instance;
    private CustomBlocks blocks;
    private ModelRegistry modelRegistry;
    private ResourcePackBuilder resourcePackBuilder;

    public static NamespacedKey CUSTOM_BLOCKS_KEY;

    @Override
    public void onLoad() {
        instance = this;
        CUSTOM_BLOCKS_KEY = new NamespacedKey(OpenItems.getInstance(), "custom_blocks");
    }

    @Override
    public void onEnable() {
        this.modelRegistry = new ModelRegistry();
        this.blocks = new CustomBlocks();
        this.resourcePackBuilder = new ResourcePackBuilder();


        load();

        //Bukkit.getPluginManager().registerEvents(new ArmorDamagePreventListener(), instance);

        getCommand("oedit").setExecutor(new OEditCommand());
        getCommand("openitems").setExecutor(new OItemsCommand());

        Utils.resyncCommands();


        if (OpenItemsConfig.buildOnEnable)
            this.resourcePackBuilder.build();
    }

    public void load() {
        OpenItemsConfig.init();
        this.resourcePackBuilder.loadRegistry();
    }


    @Override
    public void onDisable() {
    }

    public static OpenItems getInstance() {
        return instance;
    }

    public CustomBlocks getBlocks() {
        return blocks;
    }

    public ModelRegistry getModelRegistry() {
        return modelRegistry;
    }

    public ResourcePackBuilder getResourcePackBuilder() {
        return resourcePackBuilder;
    }

    public static void sync(Runnable run) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(getInstance(), run);
    }


    public static void async(Runnable run) {
        Bukkit.getScheduler().runTaskAsynchronously(getInstance(), run);
    }
}

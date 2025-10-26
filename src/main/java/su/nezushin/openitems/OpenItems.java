package su.nezushin.openitems;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import su.nezushin.openitems.blocks.CustomBlocks;
import su.nezushin.openitems.cmd.OEditCommand;
import su.nezushin.openitems.cmd.OItemsCommand;
import su.nezushin.openitems.hooks.FontImageExpansion;
import su.nezushin.openitems.hooks.ResourcePackManagerHook;
import su.nezushin.openitems.hooks.RoseResourcepackHook;
import su.nezushin.openitems.rp.ResourcePackBuilder;
import su.nezushin.openitems.utils.OpenItemsConfig;
import su.nezushin.openitems.utils.Utils;

public final class OpenItems extends JavaPlugin {

    private static OpenItems instance;
    private CustomBlocks blocks;
    private ModelRegistry modelRegistry;
    private ResourcePackBuilder resourcePackBuilder;

    private FontImageExpansion papiHook;
    private ResourcePackManagerHook resourcePackManagerHook;
    private RoseResourcepackHook roseResourcepackHookHook;

    public static NamespacedKey CUSTOM_BLOCKS_CHUNK_KEY;

    public static NamespacedKey CUSTOM_BLOCKS_CHECKED_CHUNK_KEY;

    @Override
    public void onLoad() {
        instance = this;
        CUSTOM_BLOCKS_CHUNK_KEY = new NamespacedKey(OpenItems.getInstance(), "custom_blocks");
        CUSTOM_BLOCKS_CHECKED_CHUNK_KEY = new NamespacedKey(OpenItems.getInstance(), "custom_blocks_checked");
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
        if (papiHook != null)
            papiHook.unregister();
        OpenItemsConfig.init();
        this.resourcePackBuilder.loadRegistry();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            papiHook = new FontImageExpansion();
            papiHook.register();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ResourcePackManager")) {
            resourcePackManagerHook = new ResourcePackManagerHook();
            resourcePackManagerHook.register();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("RoseResourcepack")) {
            roseResourcepackHookHook = new RoseResourcepackHook();
        }
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

    public FontImageExpansion getPapiHook() {
        return papiHook;
    }

    public void callHooksBuildRP() {

        if(resourcePackManagerHook != null)
            resourcePackManagerHook.build();
        if(roseResourcepackHookHook != null)
            roseResourcepackHookHook.build();
    }
}

package su.nezushin.openitems;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import su.nezushin.openitems.blocks.CustomBlocks;
import su.nezushin.openitems.cmd.ItemEditCommand;
import su.nezushin.openitems.rp.ResourcePackBuilder;

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

        this.resourcePackBuilder.build();
        System.out.println(this.modelRegistry.getItems());
        //Bukkit.getPluginManager().registerEvents(new TestListener(), instance);


        getCommand("oedit").setExecutor(new ItemEditCommand());

        Utils.resyncCommands();
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

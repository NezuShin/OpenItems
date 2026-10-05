package su.nezushin.openitems;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import su.nezushin.openitems.blocks.CustomBlocks;
import su.nezushin.openitems.cmd.OEditCommand;
import su.nezushin.openitems.cmd.OItemsCommand;
import su.nezushin.openitems.hooks.CommandHooks;
import su.nezushin.openitems.hooks.FontImageExpansion;
import su.nezushin.openitems.inventory.PlayerInventoryBackup;
import su.nezushin.openitems.hooks.worldedit.WorldEditHook;
import su.nezushin.openitems.hooks.worldedit.WorldEditSupportState;
import su.nezushin.openitems.rp.ResourcePackBuilder;
import su.nezushin.openitems.scheduler.SchedulerManager;
import su.nezushin.openitems.utils.OpenItemsConfig;
import su.nezushin.openitems.utils.Utils;

public final class OpenItems extends JavaPlugin {

    private static OpenItems instance;
    private CustomBlocks blocks;
    private ModelRegistry modelRegistry;
    private ResourcePackBuilder resourcePackBuilder;

    private Gson gson = new Gson();

    private FontImageExpansion papiHook;
    private CommandHooks commandHooks;
    private WorldEditHook worldEditHook;
    private PlayerInventoryBackup inventoryBackup;
    private SchedulerManager scheduler;

    public static NamespacedKey CUSTOM_BLOCKS_VERSION_KEY;

    public static NamespacedKey CUSTOM_BLOCKS_BPARTS_KEY;

    public static NamespacedKey CUSTOM_BLOCKS_CHECKED_CHUNK_KEY;

    @Override
    public void onLoad() {
        instance = this;
        this.scheduler = new SchedulerManager(this);
        CUSTOM_BLOCKS_VERSION_KEY = new NamespacedKey(OpenItems.getInstance(), "custom_blocks_version");
        CUSTOM_BLOCKS_BPARTS_KEY = new NamespacedKey(OpenItems.getInstance(), "custom_blocks_bparts");
        CUSTOM_BLOCKS_CHECKED_CHUNK_KEY = new NamespacedKey(OpenItems.getInstance(), "custom_blocks_checked");
    }

    @Override
    public void onEnable() {
        this.modelRegistry = new ModelRegistry();
        this.blocks = new CustomBlocks();
        this.resourcePackBuilder = new ResourcePackBuilder();
        this.inventoryBackup = new PlayerInventoryBackup();

        this.blocks.removeAllDisplayEntities();

        load();

        //Bukkit.getPluginManager().registerEvents(new ArmorDamagePreventListener(), instance);

        getCommand("oedit").setExecutor(new OEditCommand());
        getCommand("openitems").setExecutor(new OItemsCommand());

        Utils.resyncCommands();

        this.commandHooks = new CommandHooks();

        if (OpenItemsConfig.buildOnEnable)
            this.commandHooks.startBuild(Bukkit.getConsoleSender(), false);

    }

    public void load() {
        OpenItemsConfig.init();
        this.resourcePackBuilder.loadRegistry();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            papiHook = new FontImageExpansion();
            scheduler.runGlobal(() -> papiHook.register());
        }

        if (WorldEditSupportState.isWorldEditPresent()) {
            if (worldEditHook == null)
                worldEditHook = new WorldEditHook();
            scheduler.runGlobal(() -> worldEditHook.register());
        } else if (worldEditHook != null) {
            scheduler.runGlobal(() -> worldEditHook.unregister());
        }
    }


    @Override
    public void onDisable() {
        if (worldEditHook != null)
            worldEditHook.unregister();
        if (this.blocks != null)
            this.blocks.removeAllDisplayEntities();
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

    public CommandHooks getCommandHooks() {
        return commandHooks;
    }

    public SchedulerManager getScheduler() {
        return scheduler;
    }

    public static void async(Runnable run) {
        var thread = new Thread(run);
        thread.setName("OIThread");
        thread.start();
    }

    public FontImageExpansion getPapiHook() {
        return papiHook;
    }

    public Gson getGson() {
        return gson;
    }

    public WorldEditHook getWorldEditHook() {
        return worldEditHook;
    }

    public PlayerInventoryBackup getInventoryBackup() {
        return inventoryBackup;
    }
}

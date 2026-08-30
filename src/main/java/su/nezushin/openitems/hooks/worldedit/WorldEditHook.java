package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.OpenItemsEditSessionFactoryInstaller;
import com.sk89q.worldedit.OpenItemsEditSessionSupport;
import com.sk89q.worldedit.session.OpenItemsSessionManagerInstaller;
import org.bukkit.World;
import su.nezushin.openitems.OpenItems;

public final class WorldEditHook {

    private final OpenItemsBlockParser blockParser;
    private volatile boolean active;
    private boolean parserRegistered;
    private boolean eventBusRegistered;
    private boolean sessionManagerInstalled;
    private boolean editSessionFactoryInstalled;

    private final Object editSessionListener = new Object() {
        @Subscribe
        public void onEditSession(EditSessionEvent event) {
            if (!active)
                return;

            if (event.getStage() != EditSession.Stage.BEFORE_CHANGE
                    && event.getStage() != EditSession.Stage.BEFORE_HISTORY)
                return;

            if (event.getWorld() == null)
                return;

            World world = BukkitAdapter.adapt(event.getWorld());
            event.setExtent(new OpenItemsWorldEditExtent(
                    event.getExtent(), world, WorldEditHook.this::isActive, event.getStage()));
        }
    };

    public WorldEditHook() {
        this.blockParser = new OpenItemsBlockParser(WorldEdit.getInstance(), this::isActive);
    }

    public void register() {
        active = true;
        OpenItemsEditSessionSupport.setActive(this::isActive);

        WorldEdit worldEdit = WorldEdit.getInstance();
        if (!parserRegistered) {
            worldEdit.getBlockFactory().register(blockParser);
            parserRegistered = true;
        }
        if (!eventBusRegistered) {
            worldEdit.getEventBus().register(editSessionListener);
            eventBusRegistered = true;
        }
        if (!sessionManagerInstalled) {
            OpenItemsSessionManagerInstaller.install();
            sessionManagerInstalled = true;
        }
        if (!editSessionFactoryInstalled) {
            OpenItemsEditSessionFactoryInstaller.install();
            editSessionFactoryInstalled = true;
        }

        OpenItems.getInstance().getLogger().info("WorldEdit compatibility enabled");
    }

    public void unregister() {
        active = false;
        OpenItemsEditSessionSupport.setActive(() -> false);

        if (sessionManagerInstalled) {
            try {
                OpenItemsSessionManagerInstaller.uninstall();
            } catch (RuntimeException e) {
                OpenItems.getInstance().getLogger().warning("Failed to uninstall OpenItems WorldEdit session manager");
            }
            sessionManagerInstalled = false;
        }

        if (editSessionFactoryInstalled) {
            try {
                OpenItemsEditSessionFactoryInstaller.uninstall();
            } catch (RuntimeException e) {
                OpenItems.getInstance().getLogger().warning("Failed to uninstall OpenItems WorldEdit edit session factory");
            }
            editSessionFactoryInstalled = false;
        }

        OpenItemsWorldEditWorlds.clearCache();

        if (!eventBusRegistered)
            return;

        try {
            WorldEdit.getInstance().getEventBus().unregister(editSessionListener);
        } catch (IllegalArgumentException ignored) {
            // Already unregistered
        }
        eventBusRegistered = false;
    }

    boolean isActive() {
        return active;
    }
}

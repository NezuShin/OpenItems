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
import su.nezushin.openitems.utils.OpenItemsConfig;

import java.util.logging.Level;

/**
 * Registers tiered WorldEdit integration:
 * <ul>
 *   <li>Basic — extent writes + {@code oi:} block parser</li>
 *   <li>Extended — vanilla WE world wrapper and session/factory overrides for //copy</li>
 *   <li>FAWE — {@link OpenItemsFaweExtent} for reads and //set; vanilla extent is not used</li>
 * </ul>
 */
public final class WorldEditHook {

    private final WorldEditPresetStore presetStore = new WorldEditPresetStore();
    private final OpenItemsBlockParser blockParser;
    private boolean parserRegistered;
    private boolean eventBusRegistered;
    private boolean sessionManagerInstalled;
    private boolean editSessionFactoryInstalled;

    private final Object editSessionListener = new Object() {
        @Subscribe
        public void onEditSession(EditSessionEvent event) {
            if (!WorldEditSupportState.isBasicActive())
                return;

            if (event.getStage() != EditSession.Stage.BEFORE_CHANGE
                    && event.getStage() != EditSession.Stage.BEFORE_HISTORY)
                return;

            if (event.getWorld() == null)
                return;

            World world = BukkitAdapter.adapt(event.getWorld());
            if (WorldEditSupportState.isFaweMode()) {
                event.setExtent(new OpenItemsFaweExtent(
                        event.getExtent(),
                        world,
                        WorldEditSupportState::isBasicActive,
                        event.getStage()));
                return;
            }

            event.setExtent(new OpenItemsWorldEditExtent(
                    event.getExtent(),
                    world,
                    WorldEditSupportState::isBasicActive,
                    event.getStage()));
        }
    };

    public WorldEditHook() {
        this.blockParser = new OpenItemsBlockParser(
                WorldEdit.getInstance(), WorldEditSupportState::isBasicActive, presetStore);
    }

    public WorldEditPresetStore getPresetStore() {
        return presetStore;
    }

    public void register() {
        var logger = OpenItems.getInstance().getLogger();

        if (!OpenItemsConfig.worldEditBasicSupport) {
            unregister();
            logger.info("WorldEdit compatibility disabled (worldedit.enable-basic-support=false)");
            return;
        }

        if (!WorldEditSupportState.isWorldEditPresent()) {
            unregister();
            return;
        }

        boolean fawePresent = WorldEditSupportState.isFawePresent();
        boolean faweMode = fawePresent && OpenItemsConfig.worldEditEnableFawe;
        boolean extendedMode = OpenItemsConfig.worldEditExtendedSupport && !faweMode;

        if (fawePresent && OpenItemsConfig.worldEditExtendedSupport && !OpenItemsConfig.worldEditEnableFawe) {
            logger.warning(
                    "worldedit.enable-extended-support is not compatible with FastAsyncWorldEdit. "
                            + "Set worldedit.enable-fawe=true (recommended) or disable extended support.");
        }

        WorldEditSupportState.configure(true, extendedMode, faweMode);
        OpenItemsEditSessionSupport.setExtendedActive(WorldEditSupportState::shouldWrapWorld);

        WorldEdit worldEdit = WorldEdit.getInstance();
        if (!parserRegistered) {
            worldEdit.getBlockFactory().register(blockParser);
            parserRegistered = true;
        }
        if (!eventBusRegistered) {
            worldEdit.getEventBus().register(editSessionListener);
            eventBusRegistered = true;
        }

        if (extendedMode) {
            installExtendedOverrides(logger);
        } else {
            uninstallExtendedOverrides(logger);
        }

        if (faweMode) {
            logger.info("WorldEdit compatibility enabled (basic + FAWE reads and //set)");
        } else if (extendedMode) {
            logger.warning(
                    "WorldEdit compatibility enabled (basic + extended). "
                            + "Extended mode uses internal overrides and may break with some WorldEdit versions. "
                            + "Disable worldedit.enable-extended-support if you encounter issues.");
        } else {
            logger.info("WorldEdit compatibility enabled (basic — //set and overwrite cleanup only)");
            if (fawePresent && !OpenItemsConfig.worldEditEnableFawe) {
                logger.info("FastAsyncWorldEdit detected — set worldedit.enable-fawe=true for //copy and schematic support.");
            }
        }
    }

    public void unregister() {
        WorldEditSupportState.disable();
        OpenItemsEditSessionSupport.setExtendedActive(() -> false);

        uninstallExtendedOverrides(null);
        OpenItemsWorldEditWorlds.clearCache();
        presetStore.clearAll();

        if (!eventBusRegistered)
            return;

        try {
            WorldEdit.getInstance().getEventBus().unregister(editSessionListener);
        } catch (IllegalArgumentException ignored) {
            // Already unregistered
        }
        eventBusRegistered = false;
    }

    private void installExtendedOverrides(java.util.logging.Logger logger) {
        if (!sessionManagerInstalled) {
            try {
                OpenItemsSessionManagerInstaller.install();
                sessionManagerInstalled = true;
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, "Failed to install OpenItems WorldEdit session manager", e);
            }
        }
        if (!editSessionFactoryInstalled) {
            try {
                OpenItemsEditSessionFactoryInstaller.install();
                editSessionFactoryInstalled = true;
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, "Failed to install OpenItems WorldEdit edit session factory", e);
            }
        }
    }

    private void uninstallExtendedOverrides(java.util.logging.Logger logger) {
        if (sessionManagerInstalled) {
            try {
                OpenItemsSessionManagerInstaller.uninstall();
            } catch (RuntimeException e) {
                if (logger != null)
                    logger.warning("Failed to uninstall OpenItems WorldEdit session manager");
            }
            sessionManagerInstalled = false;
        }

        if (editSessionFactoryInstalled) {
            try {
                OpenItemsEditSessionFactoryInstaller.uninstall();
            } catch (RuntimeException e) {
                if (logger != null)
                    logger.warning("Failed to uninstall OpenItems WorldEdit edit session factory");
            }
            editSessionFactoryInstalled = false;
        }
    }
}

package su.nezushin.openitems.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Schedules work on the server thread (Paper) or on Folia's global region and per-location region threads.
 * Callers use this type only. The active {@link OpenItemsScheduler} is chosen once at construction.
 */
public final class SchedulerManager {

    private final OpenItemsScheduler scheduler;

    public SchedulerManager(Plugin plugin) {
        this.scheduler = detectFolia() ? new FoliaScheduler(plugin) : new PaperScheduler(plugin);
    }

    public void runGlobal(Runnable task) {
        scheduler.runGlobal(task);
    }

    public ScheduledWork runGlobalDelayed(Runnable task, long delayTicks) {
        return scheduler.runGlobalDelayed(task, delayTicks);
    }

    public void executeGlobal(Runnable task) {
        scheduler.executeGlobal(task);
    }

    public void runAt(Location location, Runnable task) {
        scheduler.runAt(location, task);
    }

    public ScheduledWork runAtDelayed(Location location, Runnable task, long delayTicks) {
        return scheduler.runAtDelayed(location, task, delayTicks);
    }

    public boolean owns(Location location) {
        return scheduler.owns(location);
    }

    public void runOnEntity(Entity entity, Runnable task) {
        scheduler.runOnEntity(entity, task);
    }

    public boolean isFolia() {
        return scheduler.isFolia();
    }

    private static boolean detectFolia() {
        return "Folia".equals(Bukkit.getName());
    }

    public interface OpenItemsScheduler {
        void runGlobal(Runnable task);

        ScheduledWork runGlobalDelayed(Runnable task, long delayTicks);

        void executeGlobal(Runnable task);

        void runAt(Location location, Runnable task);

        ScheduledWork runAtDelayed(Location location, Runnable task, long delayTicks);

        boolean owns(Location location);

        void runOnEntity(Entity entity, Runnable task);

        boolean isFolia();
    }

    public interface ScheduledWork {
        void cancel();
    }

    private static final class PaperScheduler implements OpenItemsScheduler {

        private final Plugin plugin;

        private PaperScheduler(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void runGlobal(Runnable task) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task);
        }

        @Override
        public ScheduledWork runGlobalDelayed(Runnable task, long delayTicks) {
            int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task, delayTicks);
            return () -> Bukkit.getScheduler().cancelTask(taskId);
        }

        @Override
        public void executeGlobal(Runnable task) {
            if (Bukkit.isPrimaryThread())
                task.run();
            else
                runGlobal(task);
        }

        @Override
        public void runAt(Location location, Runnable task) {
            runGlobal(task);
        }

        @Override
        public ScheduledWork runAtDelayed(Location location, Runnable task, long delayTicks) {
            return runGlobalDelayed(task, delayTicks);
        }

        @Override
        public boolean owns(Location location) {
            return Bukkit.isPrimaryThread();
        }

        @Override
        public void runOnEntity(Entity entity, Runnable task) {
            executeGlobal(task);
        }

        @Override
        public boolean isFolia() {
            return false;
        }
    }

    private static final class FoliaScheduler implements OpenItemsScheduler {

        private final Plugin plugin;

        private FoliaScheduler(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void runGlobal(Runnable task) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduled -> task.run());
        }

        @Override
        public ScheduledWork runGlobalDelayed(Runnable task, long delayTicks) {
            ScheduledTask scheduled = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, ignored -> task.run(), delayTicks);
            return scheduled::cancel;
        }

        @Override
        public void executeGlobal(Runnable task) {
            if (Bukkit.isGlobalTickThread())
                task.run();
            else
                runGlobal(task);
        }

        @Override
        public void runAt(Location location, Runnable task) {
            Bukkit.getRegionScheduler().run(plugin, location, scheduled -> task.run());
        }

        @Override
        public ScheduledWork runAtDelayed(Location location, Runnable task, long delayTicks) {
            ScheduledTask scheduled = Bukkit.getRegionScheduler().runDelayed(plugin, location, ignored -> task.run(), delayTicks);
            return scheduled::cancel;
        }

        @Override
        public boolean owns(Location location) {
            return Bukkit.isOwnedByCurrentRegion(location);
        }

        @Override
        public void runOnEntity(Entity entity, Runnable task) {
            if (Bukkit.isOwnedByCurrentRegion(entity)) {
                task.run();
                return;
            }

            entity.getScheduler().execute(plugin, task, null, 1L);
        }

        @Override
        public boolean isFolia() {
            return true;
        }
    }
}

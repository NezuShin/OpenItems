package su.nezushin.openitems.hooks;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.BroadcastMessageEvent;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.utils.Message;
import su.nezushin.openitems.utils.OpenItemsConfig;

import java.util.List;

/**
 * Runs configured console commands before/after a resource pack build.
 * Before-build commands run one at a time; each waits for quiet output before the next.
 */
public class CommandHooks implements Listener {

    private boolean buildInProgress;
    private boolean waitingForQuiet;
    private int quietTaskId = -1;
    private Runnable quietCallback;

    public CommandHooks() {
        Bukkit.getPluginManager().registerEvents(this, OpenItems.getInstance());
    }

    /**
     * @param sender   who triggered the build (receives status messages when {@code announce} is true)
     * @param announce whether to send start/done messages (command yes, build-on-enable no)
     */
    public void startBuild(CommandSender sender, boolean announce) {
        if (buildInProgress)
            return;

        buildInProgress = true;

        if (announce)
            Message.oi_started_build.send(sender);

        runBeforeBuildCommands(OpenItemsConfig.beforeBuildCommands, sender, () -> OpenItems.async(() -> {
            try {
                var ok = OpenItems.getInstance().getResourcePackBuilder().build();
                if (!ok) {
                    if (announce)
                        Message.oi_build_end_err.send(sender);
                    buildInProgress = false;
                    return;
                }

                if (announce)
                    Message.oi_build_end_done.send(sender);

                OpenItems.sync(() -> runAfterBuildCommands(OpenItemsConfig.afterBuildCommands, sender, () -> {
                    if (announce && !sender.equals(Bukkit.getConsoleSender())) {
                        OpenItems.getInstance().getModelRegistry().reportLoaded(sender);
                        if (OpenItems.getInstance().getResourcePackBuilder().isHasMipMapProblem())
                            Message.oi_build_mip_map_warning.send(Bukkit.getConsoleSender());
                    }
                    buildInProgress = false;
                }));
            } catch (Exception ex) {
                ex.printStackTrace();
                if (announce)
                    Message.oi_build_end_err.send(sender);
                buildInProgress = false;
            }
        }));
    }

    private void runBeforeBuildCommands(List<String> commands, CommandSender initiator, Runnable then) {
        var toRun = normalizeCommands(commands);
        if (toRun.isEmpty()) {
            syncRun(then);
            return;
        }

        var hookSender = createHookSender(initiator);
        runBeforeBuildCommandAt(hookSender, toRun, 0, then);
    }

    private void runBeforeBuildCommandAt(CommandSender hookSender, List<String> commands, int index, Runnable then) {
        Runnable task = () -> {
            try {
                Bukkit.dispatchCommand(hookSender, commands.get(index));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (index + 1 >= commands.size()) {
                if (OpenItemsConfig.beforeBuildMessageTimeout <= 0)
                    then.run();
                else
                    afterQuiet(then);
                return;
            }

            if (OpenItemsConfig.beforeBuildMessageTimeout <= 0)
                runBeforeBuildCommandAt(hookSender, commands, index + 1, then);
            else
                afterQuiet(() -> runBeforeBuildCommandAt(hookSender, commands, index + 1, then));
        };

        syncRun(task);
    }

    private void runAfterBuildCommands(List<String> commands, CommandSender initiator, Runnable then) {
        var toRun = normalizeCommands(commands);
        if (toRun.isEmpty()) {
            then.run();
            return;
        }

        var hookSender = createHookSender(initiator);
        for (var command : toRun) {
            try {
                Bukkit.dispatchCommand(hookSender, command);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        then.run();
    }

    private List<String> normalizeCommands(List<String> commands) {
        if (commands == null)
            return List.of();
        return commands.stream()
                .filter(c -> c != null && !c.isBlank())
                .map(c -> c.charAt(0) == '/' ? c.substring(1) : c)
                .toList();
    }

    private void syncRun(Runnable task) {
        if (Bukkit.isPrimaryThread())
            task.run();
        else
            OpenItems.sync(task);
    }

    private CommandSender createHookSender(CommandSender initiator) {
        return Bukkit.getServer().createCommandSender(component -> {
            Bukkit.getConsoleSender().sendMessage(component);
            if (initiator != null && !initiator.equals(Bukkit.getConsoleSender()))
                initiator.sendMessage(component);
            onCommandMessage();
        });
    }

    private void afterQuiet(Runnable then) {
        this.quietCallback = then;
        this.waitingForQuiet = true;
        scheduleQuietTask();
    }

    private void scheduleQuietTask() {
        if (quietTaskId != -1)
            Bukkit.getScheduler().cancelTask(quietTaskId);

        var ticks = Math.max(1L, (OpenItemsConfig.beforeBuildMessageTimeout + 49L) / 50L);
        quietTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(OpenItems.getInstance(), () -> {
            quietTaskId = -1;
            waitingForQuiet = false;
            var cb = quietCallback;
            quietCallback = null;
            if (cb != null)
                cb.run();
        }, ticks);
    }

    private void onCommandMessage() {
        if (!waitingForQuiet)
            return;
        if (!Bukkit.isPrimaryThread()) {
            OpenItems.sync(this::onCommandMessage);
            return;
        }
        scheduleQuietTask();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBroadcast(BroadcastMessageEvent event) {
        onCommandMessage();
    }
}

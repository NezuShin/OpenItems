package su.nezushin.openitems.hooks.worldedit;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import su.nezushin.openitems.utils.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FAWE drops {@link OpenItemsFaweExtent} unless {@code extent.allowed-plugins} contains its class name.
 * Checked when WorldEdit support is registered (startup and {@code /oi reload}).
 */
final class OpenItemsFaweAllowlist {

    static final String EXTENT_CLASS = OpenItemsFaweExtent.class.getName();
    private static final String PERMISSION = "nezu.openitems.openitems";

    private OpenItemsFaweAllowlist() {
    }

    static void warnIfExtentBlocked(Logger logger) {
        if (!WorldEditSupportState.isFaweMode())
            return;

        if (read(logger) != Allowlist.BLOCKS)
            return;

        logger.warning("FastAsyncWorldEdit is blocking OpenItems edits.");
        logger.warning("Add this line under extent.allowed-plugins in plugins/FastAsyncWorldEdit/config.yml, then run /fawe reload:");
        logger.warning("- \"" + EXTENT_CLASS + "\"");
        notifyPlayers();
    }

    /**
     * Same rule as FAWE's {@code EditSessionBuilder}: the extent class name must contain an allowlist
     * entry, case-insensitively.
     */
    static boolean isListed(String className, List<String> allowed) {
        String name = className.toLowerCase(Locale.ROOT);
        for (String entry : allowed) {
            if (entry != null && name.contains(entry.toLowerCase(Locale.ROOT)))
                return true;
        }
        return false;
    }

    private static Allowlist read(Logger logger) {
        try {
            List<String> allowed = allowedPlugins();
            if (allowed == null) {
                logger.warning("Could not read FastAsyncWorldEdit extent.allowed-plugins. "
                        + "If OpenItems edits are ignored, add \"" + EXTENT_CLASS
                        + "\" under extent.allowed-plugins and run /we reload.");
                return Allowlist.UNKNOWN;
            }
            return isListed(EXTENT_CLASS, allowed) ? Allowlist.ALLOWS : Allowlist.BLOCKS;
        } catch (ReflectiveOperationException | LinkageError e) {
            logger.log(Level.WARNING,
                    "Could not read FastAsyncWorldEdit extent.allowed-plugins. "
                            + "If OpenItems edits are ignored, add \"" + EXTENT_CLASS
                            + "\" under extent.allowed-plugins and run /we reload.",
                    e);
            return Allowlist.UNKNOWN;
        }
    }

    private static List<String> allowedPlugins() throws ReflectiveOperationException {
        Plugin fawe = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");
        if (fawe == null)
            return null;

        Class<?> settingsClass = Class.forName(
                "com.fastasyncworldedit.core.configuration.Settings",
                true,
                fawe.getClass().getClassLoader());
        Object settings = settingsClass.getMethod("settings").invoke(null);
        if (settings == null)
            return null;

        Object extent = settingsClass.getField("EXTENT").get(settings);
        if (extent == null)
            return null;

        Object raw = extent.getClass().getField("ALLOWED_PLUGINS").get(extent);
        if (!(raw instanceof List<?> list))
            return null;

        List<String> entries = new ArrayList<>(list.size());
        for (Object entry : list) {
            if (entry == null)
                continue;
            if (!(entry instanceof String value))
                return null;
            entries.add(value);
        }
        return entries;
    }

    private static void notifyPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(PERMISSION))
                send(player);
        }
    }

    private static void send(CommandSender sender) {
        if (Message.oi_we_fawe_extent_blocked.hasText()) {
            Message.oi_we_fawe_extent_blocked.replace("{class}", EXTENT_CLASS).send(sender);
            return;
        }

        MiniMessage mini = MiniMessage.miniMessage();
        sender.sendMessage(mini.deserialize("<red>FastAsyncWorldEdit is blocking OpenItems edits."));
        sender.sendMessage(mini.deserialize(
                "<gray>Add this line under <white>extent.allowed-plugins<gray> in "
                        + "<white>plugins/FastAsyncWorldEdit/config.yml<gray>, then run <white>/we reload<gray>:"));
        sender.sendMessage(mini.deserialize(
                "<#08c0c2><click:copy_to_clipboard:'" + EXTENT_CLASS + "'>- \"" + EXTENT_CLASS + "\""));
    }

    private enum Allowlist {
        ALLOWS, BLOCKS, UNKNOWN
    }
}

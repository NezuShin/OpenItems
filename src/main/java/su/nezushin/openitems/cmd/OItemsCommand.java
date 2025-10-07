package su.nezushin.openitems.cmd;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.utils.Message;
import su.nezushin.openitems.utils.OpenItemsConfig;
import su.nezushin.openitems.utils.Utils;

import java.io.IOException;
import java.util.List;

public class OItemsCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender.hasPermission("nezu.openitems.openitems"))) {

            Message.err_u_dont_have_permission.send(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("build")) {
            Message.started_build.send(sender);
            OpenItems.async(() -> {
                if (OpenItems.getInstance().getResourcePackBuilder().build()) {


                    Message.build_end_done.send(sender);
                    OpenItems.sync(() -> {
                        if (!sender.equals(Bukkit.getConsoleSender())) {
                            OpenItems.getInstance().getModelRegistry().reportLoaded(sender);
                            if (OpenItems.getInstance().getResourcePackBuilder().isHasMipMapProblem())
                                Message.build_mip_map_warning.send(Bukkit.getConsoleSender());
                        }
                    });
                    return;
                }
                Message.build_end_err.send(sender);
            });

            return true;
        } else if (args[0].equalsIgnoreCase("reload")) {
            OpenItems.async(() -> {
                try {
                    Message.config_load_start.send(sender);
                    OpenItems.getInstance().load();
                    Message.config_load_success.send(sender);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Message.config_load_err.send(sender);
                }
            });
        } else if (args[0].equalsIgnoreCase("emoji")) {
            if (args.length > 1) {
                sender.sendMessage(args[1]);
                return true;
            }
        } else if (args[0].equalsIgnoreCase("scan-mip-map")) {
            OpenItems.async(() -> {
                try {
                    var wrongFiles = Utils.checkMipMap();

                    if (wrongFiles.isEmpty()) {
                        Message.mip_map_end_not_found.send(sender);
                        return;
                    }

                    Message.mip_map_end_done.send(sender);
                    for (var i : wrongFiles)
                        Message.mip_map_limitation.replace(
                                "{height}", String.valueOf(i.height()),
                                "{width}", String.valueOf(i.width()),
                                "{file}", Utils.getFileAsNamespacedPath(i.file())).send(sender);
                } catch (IOException e) {
                    Message.mip_map_end_err.send(sender);
                    e.printStackTrace();
                }
            });
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 1) {
            return Lists.newArrayList("build", "emoji", "reload", "scan-mip-map")
                    .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[0])).toList();
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("emoji")) {
                return OpenItems.getInstance().getModelRegistry().getFontImages().values()
                        .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[1])).toList();
            }
        }

        return List.of();
    }
}

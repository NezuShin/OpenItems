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
import su.nezushin.openitems.utils.Utils;

import java.io.IOException;
import java.util.List;

public class OItemsCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        try {
            if (!(sender.hasPermission("nezu.openitems.openitems"))) {

                Message.err_u_dont_have_permission.send(sender);
                return true;
            }

            if (args.length == 0) {
                Message.oi_help_general.send(sender);
                return true;
            }

            if (args[0].equalsIgnoreCase("build")) {
                Message.oi_started_build.send(sender);
                OpenItems.async(() -> {
                    if (OpenItems.getInstance().getResourcePackBuilder().build()) {


                        Message.oi_build_end_done.send(sender);
                        OpenItems.sync(() -> {
                            if (!sender.equals(Bukkit.getConsoleSender())) {
                                OpenItems.getInstance().getModelRegistry().reportLoaded(sender);
                                if (OpenItems.getInstance().getResourcePackBuilder().isHasMipMapProblem())
                                    Message.oi_build_mip_map_warning.send(Bukkit.getConsoleSender());
                            }
                        });
                        return;
                    }
                    Message.oi_build_end_err.send(sender);
                });

                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                OpenItems.async(() -> {
                    try {
                        Message.oi_config_load_start.send(sender);
                        OpenItems.getInstance().load();
                        Message.oi_config_load_success.send(sender);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        Message.oi_config_load_err.send(sender);
                    }
                });
            }
            if (args[0].equalsIgnoreCase("font")) {
                System.out.println(args.length);
                if (args.length > 2) {
                    if (args[1].equalsIgnoreCase("print_image")) {
                        sender.sendMessage(args[2]);
                    } else if (args[1].equalsIgnoreCase("print_path")) {
                        var id = args[2];
                        System.out.println(id);
                        var fontImage = OpenItems.getInstance().getModelRegistry().getFontImages().get(id);

                        if (fontImage == null || fontImage.isEmpty()) {
                            Message.oi_font_image_not_found.replace("{id}", id).send(sender);
                            return true;
                        }

                        Message.oi_font_image_format.replace("{id}", id, "{font-image}", fontImage).send(sender);
                        return true;
                    } else if (args[1].equalsIgnoreCase("print_offset_sequence")) {
                        var offset = Utils.parseInt(args[2]);

                        var rawSequence = Utils.getOffset(offset);
                        var sequence = Utils.unicodeToEscapeSequence(rawSequence);

                        Message.oi_font_offset_format.replace("{sequence}", sequence,
                                "{raw-sequence}", rawSequence, "{offset}", String.valueOf(offset)).send(sender);
                        return true;
                    }
                }
            } else if (args[0].equalsIgnoreCase("scan_mip_map")) {
                OpenItems.async(() -> {
                    try {
                        var wrongFiles = Utils.checkMipMap();

                        if (wrongFiles.isEmpty()) {
                            Message.oi_mip_map_end_not_found.send(sender);
                            return;
                        }

                        Message.oi_mip_map_end_done.send(sender);
                        for (var i : wrongFiles)
                            Message.oi_mip_map_limitation.replace(
                                    "{height}", String.valueOf(i.height()),
                                    "{width}", String.valueOf(i.width()),
                                    "{file}", Utils.getFileAsNamespacedPath(i.file())).send(sender);
                    } catch (IOException e) {
                        Message.oi_mip_map_end_err.send(sender);
                        e.printStackTrace();
                    }
                });
            }
        } catch (CommandException ex) {
            ex.send(sender);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 1)
            return Lists.newArrayList("build", "font", "reload", "scan_mip_map")
                    .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[0])).toList();

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("font"))
                return Lists.newArrayList("print_path", "print_image", "print_offset_sequence")
                        .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[1])).toList();
        } else if (args.length == 3) {
            if(args[0].equalsIgnoreCase("font")){
                if(args[1].equalsIgnoreCase("print_path"))
                    return OpenItems.getInstance().getModelRegistry().getFontImages().keySet()
                            .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();
                else if(args[1].equalsIgnoreCase("print_image"))
                    return OpenItems.getInstance().getModelRegistry().getFontImages().values()
                            .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();

            }
        }

        return List.of();
    }
}

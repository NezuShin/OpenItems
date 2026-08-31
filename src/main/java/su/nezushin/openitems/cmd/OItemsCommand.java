package su.nezushin.openitems.cmd;

import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.hooks.worldedit.WorldEditPresetStore;
import su.nezushin.openitems.utils.Message;
import su.nezushin.openitems.utils.NBTUtil;
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
                OpenItems.getInstance().getCommandHooks().startBuild(sender, true);
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
                return true;
            }
            if (args[0].equalsIgnoreCase("font")) {
                if (args.length > 2) {
                    if (args[1].equalsIgnoreCase("print_image")) {
                        sender.sendMessage(args[2]);
                    } else if (args[1].equalsIgnoreCase("print_path")) {
                        var id = args[2];
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
            } else if (args[0].equalsIgnoreCase("we")) {
                handleWorldEditPreset(sender, args);
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
            return Lists.newArrayList("build", "font", "reload", "scan_mip_map", "we")
                    .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[0])).toList();

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("we"))
                return tabCompleteWorldEditPreset(sender, args);
            if (args[0].equalsIgnoreCase("font"))
                return Lists.newArrayList("print_path", "print_image", "print_offset_sequence")
                        .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[1])).toList();
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("we"))
                return tabCompleteWorldEditPreset(sender, args);
            if (args[0].equalsIgnoreCase("font")) {
                if (args[1].equalsIgnoreCase("print_path"))
                    return OpenItems.getInstance().getModelRegistry().getFontImages().keySet()
                            .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();
                if (args[1].equalsIgnoreCase("print_image"))
                    return OpenItems.getInstance().getModelRegistry().getFontImages().values()
                            .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("we")) {
            return tabCompleteWorldEditPreset(sender, args);
        }

        return List.of();
    }

    private static void handleWorldEditPreset(CommandSender sender, String[] args) throws CommandException {
        if (args.length < 3) {
            Message.oi_we_preset_help.send(sender);
            return;
        }

        WorldEditPresetStore store = requireWorldEditPresetStore();
        String action = args[2];

        if (action.equalsIgnoreCase("list")) {
            if (!(sender instanceof Player player))
                throw new CommandException(Message.err_player_only.get());

            var names = store.getNames(player.getUniqueId());
            if (names.isEmpty()) {
                Message.oi_we_preset_list_empty.send(sender);
                return;
            }

            Message.oi_we_preset_list_header.send(sender);
            for (String name : names.stream().sorted().toList())
                Message.oi_we_preset_list_entry.replace("{name}", name).send(sender);
            return;
        }

        if (!(sender instanceof Player player))
            throw new CommandException(Message.err_player_only.get());

        if (args.length < 4) {
            Message.oi_we_preset_help.send(sender);
            return;
        }

        String name;
        try {
            name = WorldEditPresetStore.normalizeName(args[3]);
        } catch (IllegalArgumentException e) {
            throw new CommandException(Message.oi_we_preset_invalid_name.replace("{name}", args[3]));
        }

        if (action.equalsIgnoreCase("save")) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            validateWorldEditPresetItem(hand);
            store.save(player.getUniqueId(), name, hand);
            Message.oi_we_preset_saved.replace("{name}", name).send(sender);
            return;
        }

        if (action.equalsIgnoreCase("delete")) {
            if (store.delete(player.getUniqueId(), name))
                Message.oi_we_preset_deleted.replace("{name}", name).send(sender);
            else
                throw new CommandException(Message.oi_we_preset_not_found.replace("{name}", name));
            return;
        }

        Message.oi_we_preset_help.send(sender);
    }

    private static List<String> tabCompleteWorldEditPreset(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Lists.newArrayList("preset")
                    .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[1])).toList();

        if (args.length == 3 && args[1].equalsIgnoreCase("preset"))
            return Lists.newArrayList("save", "delete", "list")
                    .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();

        if (args.length == 4
                && args[1].equalsIgnoreCase("preset")
                && (args[2].equalsIgnoreCase("save") || args[2].equalsIgnoreCase("delete"))
                && sender instanceof Player player) {
            var hook = OpenItems.getInstance().getWorldEditHook();
            if (hook == null)
                return List.of();
            return hook.getPresetStore().getNames(player.getUniqueId()).stream()
                    .filter(i -> StringUtil.startsWithIgnoreCase(i, args[3]))
                    .toList();
        }

        return List.of();
    }

    private static WorldEditPresetStore requireWorldEditPresetStore() throws CommandException {
        var hook = OpenItems.getInstance().getWorldEditHook();
        if (hook == null)
            throw new CommandException(Message.oi_we_not_available.get());
        return hook.getPresetStore();
    }

    private static void validateWorldEditPresetItem(ItemStack item) throws CommandException {
        if (item == null || item.getType().isAir())
            throw new CommandException(Message.err_u_should_have_item_in_hand.get());

        String id = NBTUtil.getBlockId(item);
        if (id == null)
            throw new CommandException(Message.oi_we_preset_not_custom_block.get());

        if (!OpenItems.getInstance().getModelRegistry().getBlockTypes().containsKey(id))
            throw new CommandException(Message.oi_we_preset_unknown_model.replace("{id}", id));
    }
}

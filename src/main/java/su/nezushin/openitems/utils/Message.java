package su.nezushin.openitems.utils;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Message {

    err_u_should_have_item_in_hand, err_u_dont_have_permission, err_player_only, err_nan, err_block_id_not_set,
    current_block_data;

    private List<String> list = Lists.newArrayList("");


    public static void load(FileConfiguration conf) {
        for (Message msg : values()) {
            Object obj = conf.get("messages." + msg.name().toLowerCase().replace("_", "-"));

            if (obj instanceof List) {
                msg.list = (List<String>) obj;

            } else
                msg.list = Lists.newArrayList((String) obj);

            {
                List<String> list = msg.list;

                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) != null)
                        list.set(i, applyColor(list.get(i)));
                    else {
                    }
                }

                msg.list = list;

            }
        }
    }

    public void send(CommandSender sender) {
        new ChatMessageSender(this).send(sender);
    }

    public void kick(Player player) {
        new ChatMessageSender(this).kick(player);
    }

    public ChatMessageSender replace(String... strings) {
        return new ChatMessageSender(this).replace(strings);
    }

    public ChatMessageSender get() {
        return new ChatMessageSender(this);
    }

    public static class ChatMessageSender {

        List<String> msg;

        public ChatMessageSender(Message form) {
            this.msg = new ArrayList<String>(form.list);
        }

        public ChatMessageSender replace(String... strs) {
            String replace = null;
            for (String s : strs) {
                if (replace == null) {
                    replace = s;
                    continue;
                }
                for (int i = 0; i < msg.size(); i++) {
                    msg.set(i, msg.get(i).replace(replace, s));
                }

                replace = null;
            }
            return this;
        }

        public ChatMessageSender kick(Player p) {
            if (msg.isEmpty()) {
                p.kick(Component.text(""));
            } else {
                p.kick(MiniMessage.miniMessage().deserialize(toString()));
            }
            return this;
        }

        @Override
        public String toString() {

            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < msg.size(); i++) {
                if (i != 0)
                    builder.append("\n");
                builder.append(msg.get(i));
            }

            return builder.toString();

        }

        public ChatMessageSender send(CommandSender p) {
            try {
                for (String string : msg) {
                    p.sendMessage(MiniMessage.miniMessage().deserialize(string));
                }
            } catch (Exception e) {
                // DiscordAuth.severe("While sending message occured exception", e);
            }
            return this;
        }

    }

    public static String translateCodes(String s) {
        Pattern pattern = Pattern.compile("(§x(§[a-fA-f0-9]){6})");
        Matcher matcher = pattern.matcher("" + s);
        while (matcher.find()) {
            String color = s.substring(matcher.start(), matcher.end());
            s = s.replace(color, "<#" + color.replace("§", "").substring(1) + ">");
        }
        for (var i : ChatColor.values()) {
            for (var code : new char[]{ChatColor.COLOR_CHAR, '&'}) {
                String color = code + "" + i.getChar();
                s = s.replace(color, "<" + i.name().toLowerCase() + ">");
            }
        }
        return s;
    }

    public static String hexColor(String text) {
        Pattern pattern = Pattern.compile("#[a-fA-f0-9]{6}");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String color = text.substring(matcher.start(), matcher.end());
            text = text.replace(color, net.md_5.bungee.api.ChatColor.of(color) + "");
        }

        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String stripColor(String s) {
        var STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(ChatColor.COLOR_CHAR) + "[0-9A-FK-ORX]");
        if (s == null) {
            return null;
        }
        return STRIP_COLOR_PATTERN.matcher(s).replaceAll("");
    }

    public static String applyColor(String message) {
        return translateCodes(message);
    }

}

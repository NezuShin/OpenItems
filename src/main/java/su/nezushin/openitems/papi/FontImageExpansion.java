package su.nezushin.openitems.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.utils.Utils;

public class FontImageExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "openitems";
    }

    @Override
    public @NotNull String getAuthor() {
        return "NezuShin";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (StringUtil.startsWithIgnoreCase(params, "emoji_")) {


            var str = params.replaceFirst("emoji_", "");

            var fontImage = OpenItems.getInstance().getModelRegistry()
                    .getFontImages().get(str);

            if (fontImage != null && !fontImage.isEmpty())
                return fontImage;


            return OpenItems.getInstance().getModelRegistry().getFontImages().get(str.replace(":", "_")
                    .replace("/", "_"));
        } else if (StringUtil.startsWithIgnoreCase(params, "offset")) {
            var str = params.replaceFirst("offset_", "").replace("+", "");
            try {
                return Utils.getOffset(Integer.parseInt(str));
            } catch (NumberFormatException ex) {
                return "NaN";
            }
        }

        return null;
    }
}

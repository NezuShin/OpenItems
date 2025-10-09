package su.nezushin.openitems.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nezushin.openitems.OpenItems;

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
            return OpenItems.getInstance().getModelRegistry()
                    .getFontImages().get(params.replaceFirst("emoji_", ""));
        }

        return null;
    }
}

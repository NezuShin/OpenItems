package su.nezushin.openitems.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import su.nezushin.openitems.OpenItems;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class OpenItemsConfig {

    public static FileConfiguration config;

    public static List<String> resourcePackCopyDestinations;

    public static boolean replaceTripwiresOnChunkLoad = true, replaceChorusPlantsOnChunkLoad = true,
            enableTripwires = true, enableChorus = true, buildOnEnable, disableMipMapWarning;

    public static void init() {
        var plugin = OpenItems.getInstance();
        if (!new File(plugin.getDataFolder() + File.separator + "config.yml").exists()) {
            plugin.getConfig().options().copyDefaults(true);
            plugin.saveDefaultConfig();
        }


        config = plugin.getConfig();

        replaceTripwiresOnChunkLoad = config.getBoolean("blocks.replace-tripwires-on-chunk-load", true);
        replaceChorusPlantsOnChunkLoad = config.getBoolean("blocks.replace-chorus-plants-on-chunk-load", true);
        enableTripwires = config.getBoolean("blocks.enable-tripwires", true);
        enableChorus = config.getBoolean("blocks.enable-chorus", true);

        buildOnEnable = config.getBoolean("resourcepack.build-on-enable", true);

        disableMipMapWarning = config.getBoolean("disable-mip-map-warning", false);


        resourcePackCopyDestinations = config.getStringList("resourcepack.copy-destinations");

        var messages = new File(plugin.getDataFolder() + File.separator + "messages.yml");
        if (!messages.exists()) {
            plugin.saveResource("messages.yml", true);
        }
        Message.load(YamlConfiguration.loadConfiguration(messages));
    }

    public static List<File> getResourcePackCopyDestinationFiles() {
        return resourcePackCopyDestinations.stream().map(i -> Path.of(i).isAbsolute() ? new File(i) : new File(Bukkit.getPluginsFolder(), i)).toList();
    }
}

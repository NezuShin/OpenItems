package su.nezushin.openitems.inventory;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * YAML kits under {@code contents/<namespace>/saved_inventories/<name>.yml}.
 */
public final class SavedInventoryStore {

    public static final String DIR_NAME = "saved_inventories";
    public static final String FILE_HEADER = "This file is not intended to be edited manually. Please read about /oi inv save/load commands";

    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-zA-Z0-9._-]+");
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_-]+");

    private SavedInventoryStore() {
    }

    public record InventoryId(String namespace, String name) {
        public String toId() {
            return namespace + ":" + name;
        }
    }

    public static InventoryId parseId(String raw) {
        if (raw == null || raw.isBlank())
            throw new IllegalArgumentException("Inventory id is empty");

        int colon = raw.indexOf(':');
        if (colon <= 0 || colon != raw.lastIndexOf(':') || colon == raw.length() - 1)
            throw new IllegalArgumentException("Invalid inventory id: " + raw);

        String namespace = raw.substring(0, colon);
        String name = raw.substring(colon + 1);
        if (!NAMESPACE_PATTERN.matcher(namespace).matches() || !NAME_PATTERN.matcher(name).matches())
            throw new IllegalArgumentException("Invalid inventory id: " + raw);

        return new InventoryId(namespace, name);
    }

    public static File getNamespaceDir(String namespace) {
        return new File(OpenItems.getInstance().getResourcePackBuilder().getContentsDirectory(), namespace);
    }

    public static boolean namespaceExists(String namespace) {
        return getNamespaceDir(namespace).isDirectory();
    }

    public static File getFile(InventoryId id) {
        return new File(new File(getNamespaceDir(id.namespace()), DIR_NAME), id.name() + ".yml");
    }

    public static void scanInto(Map<String, ItemStack[]> target) {
        target.clear();

        File contents = OpenItems.getInstance().getResourcePackBuilder().getContentsDirectory();
        if (!contents.isDirectory())
            return;

        File[] namespaces = contents.listFiles();
        if (namespaces == null)
            return;

        for (File namespaceDir : namespaces) {
            if (!namespaceDir.isDirectory())
                continue;

            File dir = new File(namespaceDir, DIR_NAME);
            if (!dir.isDirectory())
                continue;

            File[] files = dir.listFiles();
            if (files == null)
                continue;

            for (File file : files) {
                if (!file.isFile() || !file.getName().endsWith(".yml"))
                    continue;

                String name = file.getName().substring(0, file.getName().length() - 4);
                if (!NAME_PATTERN.matcher(name).matches()) {
                    OpenItems.getInstance().getLogger().warning("Skipping saved inventory with invalid name: " + file.getPath());
                    continue;
                }

                try {
                    ItemStack[] items = read(file);
                    target.put(namespaceDir.getName() + ":" + name, items);
                } catch (Exception e) {
                    OpenItems.getInstance().getLogger().log(Level.WARNING,
                            "Failed to load saved inventory " + file.getPath(), e);
                }
            }
        }
    }

    public static ItemStack[] read(File file) throws IOException, InvalidConfigurationException {
        YamlConfiguration conf = new YamlConfiguration();
        conf.load(file);

        List<?> list = conf.getList("items");
        if (list == null)
            return new ItemStack[0];

        ItemStack[] items = new ItemStack[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object entry = list.get(i);
            if (entry instanceof ItemStack stack)
                items[i] = stack.getType().isAir() ? null : stack.clone();
        }
        return items;
    }

    public static void write(File file, ItemStack[] contents) throws IOException {
        file.getParentFile().mkdirs();

        YamlConfiguration conf = new YamlConfiguration();
        conf.options().setHeader(List.of(FILE_HEADER));
        conf.set("items", toYamlList(contents));
        conf.save(file);
    }

    public static ItemStack[] cloneContents(ItemStack[] contents) {
        if (contents == null)
            return new ItemStack[0];

        ItemStack[] clone = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            clone[i] = (stack == null || stack.getType().isAir()) ? null : stack.clone();
        }
        return clone;
    }

    public static ItemStack[] copyToSize(ItemStack[] contents, int size) {
        ItemStack[] out = new ItemStack[size];
        if (contents == null)
            return out;

        int n = Math.min(contents.length, size);
        for (int i = 0; i < n; i++) {
            ItemStack stack = contents[i];
            out[i] = (stack == null || stack.getType().isAir()) ? null : stack.clone();
        }
        return out;
    }

    private static List<ItemStack> toYamlList(ItemStack[] contents) {
        List<ItemStack> list = new ArrayList<>(contents.length);
        for (ItemStack stack : contents)
            list.add((stack == null || stack.getType().isAir()) ? null : stack.clone());
        return list;
    }
}

package su.nezushin.openitems.utils;

import com.google.common.collect.Comparators;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.codehaus.plexus.util.FileUtils;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.cmd.CommandException;

import javax.imageio.ImageIO;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {


    @SafeVarargs
    public static <T> List<T> concatLists(List<T>... lists) {
        List<T> r = new ArrayList<>();

        for (var i : lists) {
            r.addAll(i);
        }

        return r;
    }

    public static BlockFace[] getMainBlockFaces() {
        return new BlockFace[]{BlockFace.DOWN, BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
    }

    public static void copyFolder(File src, File dest, File parent, List<String> ignoreDirs, List<String> ignoreExtensions) throws IOException {
        if (src == null || ignoreDirs.contains(src.getAbsolutePath()
                .replace(parent.getAbsolutePath(), "")
                .replace(File.separator, "/")))
            return;

        if (src.isDirectory()) {
            String files[] = src.list();

            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);

                copyFolder(srcFile, destFile, parent, ignoreDirs, ignoreExtensions);
            }
        } else {
            src.getParentFile().mkdirs();
            var ext = src.getName();//get extension of file
            ext = ext.substring(ext.indexOf("."));
            if (ignoreExtensions.contains(ext))
                return;

            FileUtils.copyFile(src, dest);
        }
    }

    public static void resyncCommands() {
        try {
            var method = Bukkit.getServer().getClass().getDeclaredMethod("syncCommands");
            method.setAccessible(true);
            method.invoke(Bukkit.getServer());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String createPath(String path, String file) {
        return path + (path.isEmpty() ? "" : "/") + file;
    }

    public static String createPath(String path, File file) {
        return path + (path.isEmpty() ? "" : "/") + file.getName();
    }

    public static String getFileName(File file) {
        return file.getName().substring(0, file.getName().lastIndexOf("."));
    }

    public static String intToUnicodeEscapeSequence(int codepoint) {
        return String.format("\\u%04X", codepoint);
    }

    public static String charToUnicodeEscapeSequence(char c) {
        return intToUnicodeEscapeSequence(c);
    }

    public static char unicodeEscapeSequenceToChar(String sequence) {
        return (char) unicodeEscapeSequenceToInt(sequence);
    }

    public static int unicodeEscapeSequenceToInt(String sequence) {
        return Integer.parseInt(sequence.substring(2), 16);
    }

    public static String unicodeToEscapeSequence(String unicodeStr){
        StringBuilder str = new StringBuilder();
        for(var i : unicodeStr.toCharArray())
            str.append(charToUnicodeEscapeSequence(i));
        return str.toString();
    }


    /**
     * Check if texture pack has texture that limits mip map level.
     * read <a href="https://gist.github.com/HalbFettKaese/c193caeccc94b793b29981aa38170ea6">...</a>
     *
     * @return list of "wrong" texture files
     * @throws IOException
     */
    public static List<MipMapFile> checkMipMap() throws IOException {
        var returnList = new ArrayList<MipMapFile>();
        for (var i : OpenItems.getInstance().getResourcePackBuilder().getAllTextures()
                .stream().filter(i -> !i.getAbsolutePath().contains("/font/")).toList()) {
            var image = ImageIO.read(i);

            if (!isPowerOfTwo(image.getHeight()) || !isPowerOfTwo(image.getWidth())) {
                var hasMcmeta = new File(i.getParent(), i.getName() + ".mcmeta").exists();

                if (hasMcmeta && ((((double) image.getHeight()) / ((double) image.getWidth())) % 1) != 0)
                    returnList.add(new MipMapFile(i, image.getHeight(), image.getWidth()));
            }
        }
        return returnList;
    }

    /**
     * convert File (like OpenItems/contents/namespace/textures/item/test.png) to string namespace:textures/item/test.png
     *
     * @param file
     * @return
     */
    public static String getFileAsNamespacedPath(File file) {

        var contentsDir = OpenItems.getInstance().getResourcePackBuilder().getContentsDirectory();


        var relPath = file.getAbsolutePath().replace(contentsDir.getAbsolutePath(), "")
                .replace(File.separator, "/");

        if (relPath.startsWith("/"))
            relPath = relPath.substring(1);
        var index = relPath.indexOf("/");
        var namespace = relPath.substring(0, index);

        return namespace + ":" + relPath.substring(index + 1);
    }

    public static record MipMapFile(File file, int height, int width) {
    }

    public static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }


    /**
     * Extract height and ascent data from font image file
     *
     * @param fileName - file name from what take data
     * @return map with h and a keys
     */
    public static Map<String, Integer> extractNumbers(String fileName) {
        Map<String, Integer> result = new HashMap<>();


        Pattern pattern = Pattern.compile("([ha])(\\d+)");
        Matcher matcher = pattern.matcher(fileName);

        while (matcher.find()) {
            String key = matcher.group(1);
            int value = Integer.parseInt(matcher.group(2));

            result.put(key, value);
        }

        return result;
    }

    public static String getOffset(int offset) {
        var fontSpaces = OpenItems.getInstance().getModelRegistry().getFontSpaces();
        if(offset == 0)
            return "";

        var multiplier = offset > 0 ? 1 : -1;
        offset = Math.abs(offset);
        var list = fontSpaces.keySet().stream().filter(i -> i > 0).sorted().toList().reversed();
        StringBuilder str = new StringBuilder();

        while (offset > 0) {
            for (var i : list) {
                if (i <= offset) {
                    offset = offset - i;
                    str.append(fontSpaces.get(multiplier * i));
                    break;
                }
            }
        }


        return str.toString();
    }

    public static String formatMinimessage(Component component) {
        return component == null ? "null" : (MiniMessage.miniMessage().serialize(component) +
                (component instanceof TranslatableComponent ? (" " + Message.translatable_component.get()) : ""));
    }

    public static int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            throw new CommandException(Message.err_nan.replace("{nan}", str));
        }
    }

    public static float parseFloat(String str) {
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException ex) {
            throw new CommandException(Message.err_nan.replace("{nan}", str));
        }
    }
}

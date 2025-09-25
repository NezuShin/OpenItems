package su.nezushin.openitems;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.codehaus.plexus.util.FileUtils;
import su.nezushin.openitems.blocks.BlockStore;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Utils {


    @SafeVarargs
    public static <T> List<T> concatLists(List<T>... lists) {
        List<T> r = new ArrayList<>();

        for (var i : lists) {
            r.addAll(i);
        }

        return r;
    }

    public static BlockFace[] getBlockFacesForTripwire() {
        return new BlockFace[]{BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
    }

    public static BlockFace[] getBlockFacesForChorus() {
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

    public static BlockVector createBlockVector(BlockStore b) {
        return new BlockVector(b.getX(), b.getY(), b.getZ());
    }

    public static BlockVector createBlockVector(Block b) {
        return new BlockVector(b.getX(), b.getY(), b.getZ());
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

}

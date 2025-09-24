package su.nezushin.openitems;

import org.bukkit.block.BlockFace;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
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

}

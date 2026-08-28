package su.nezushin.openitems.rp.textures;

import su.nezushin.openitems.utils.Utils;

import java.io.File;

public record ResourcePackScanFile(File file, String path, String name) {

    public String pathAndName() {
        return Utils.createPath(path, name);
    }
}

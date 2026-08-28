package su.nezushin.openitems.rp.textures;

import su.nezushin.openitems.rp.NamespacedConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Face texture layouts shared by noteblocks, slabs, and stairs.
 */
public enum TextureLayout {

    SIX_FACE("up", "down", "west", "east", "south", "north"),
    COLUMN("up", "down", "side"),
    BOTTOM_SIDE_TOP("bottom", "side", "top"),
    ALL("");

    public static final List<TextureLayout> CUBE_LAYOUT_ORDER = List.of(SIX_FACE, COLUMN, ALL);
    public static final List<TextureLayout> STAIRS_LAYOUT_ORDER = List.of(BOTTOM_SIDE_TOP, ALL);

    private final Set<String> faces;

    TextureLayout(String... faces) {
        this.faces = Set.of(faces);
    }

    public Set<String> faces() {
        return faces;
    }

    public boolean isSixFace() {
        return this == SIX_FACE;
    }

    public boolean isAll() {
        return this == ALL;
    }

    public String noteblockTemplate(NamespacedConfig config) {
        return switch (this) {
            case SIX_FACE -> config.getCubeModelTemplate();
            case COLUMN -> config.getCubeSideModelTemplate();
            case ALL -> config.getCubeAllModelTemplate();
            case BOTTOM_SIDE_TOP -> throw new IllegalStateException("Stairs layout has no noteblock template");
        };
    }

    public static List<ResolvedBlockTextures> resolveAll(
            List<ResourcePackScanFile> scanFiles,
            List<TextureLayout> order,
            String namespace,
            SingleTextureExpansion singleTextureExpansion) {
        var resolved = new ArrayList<ResolvedBlockTextures>();
        for (var layout : order) {
            for (var base : new ArrayList<>(findBases(scanFiles, layout))) {
                var replacements = consumeAndBuildReplacements(
                        scanFiles, base, layout, namespace, singleTextureExpansion);
                resolved.add(new ResolvedBlockTextures(base, layout, replacements));
            }
        }
        return resolved;
    }

    private static Set<String> findBases(List<ResourcePackScanFile> scanFiles, TextureLayout layout) {
        if (layout.isAll()) {
            return new HashSet<>(scanFiles.stream().map(ResourcePackScanFile::pathAndName).toList());
        }

        return new HashSet<>(scanFiles.stream()
                .filter(i -> i.name().contains("_"))
                .filter(i -> layout.faces.contains(i.name().substring(i.name().lastIndexOf("_") + 1)))
                .map(i -> i.pathAndName().substring(0, i.pathAndName().lastIndexOf("_")))
                .filter(base -> layout.faces.stream()
                        .allMatch(face -> scanFiles.stream()
                                .anyMatch(j -> j.pathAndName().equalsIgnoreCase(base + "_" + face))))
                .toList());
    }

    private static Map<String, String> consumeAndBuildReplacements(
            List<ResourcePackScanFile> scanFiles,
            String pathAndName,
            TextureLayout layout,
            String namespace,
            SingleTextureExpansion singleTextureExpansion) {
        if (layout.isAll()) {
            var scanFile = findTexture(scanFiles, pathAndName);
            scanFiles.remove(scanFile);
            return singleTextureExpansion.expand(namespace + ":" + scanFile.pathAndName());
        }

        var replacements = new HashMap<String, String>();
        for (var face : layout.faces) {
            var scanFile = findTexture(scanFiles, pathAndName + "_" + face);
            scanFiles.remove(scanFile);
            replacements.put("{path_" + face + "}", namespace + ":" + scanFile.pathAndName());
        }
        return replacements;
    }

    private static ResourcePackScanFile findTexture(List<ResourcePackScanFile> scanFiles, String name) {
        return scanFiles.stream()
                .filter(i -> i.pathAndName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}

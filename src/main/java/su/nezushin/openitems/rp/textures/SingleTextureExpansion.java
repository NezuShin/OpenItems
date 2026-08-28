package su.nezushin.openitems.rp.textures;

import java.util.Map;

public enum SingleTextureExpansion {

    NOTEBLOCK(path -> Map.of("{path}", path)),
    STAIRS(path -> Map.of(
            "{path_bottom}", path,
            "{path_side}", path,
            "{path_top}", path)),
    SLAB(path -> Map.of(
            "{path_up}", path,
            "{path_down}", path,
            "{path_side}", path,
            "{path}", path));

    private final java.util.function.Function<String, Map<String, String>> expander;

    SingleTextureExpansion(java.util.function.Function<String, Map<String, String>> expander) {
        this.expander = expander;
    }

    public Map<String, String> expand(String texturePath) {
        return expander.apply(texturePath);
    }
}

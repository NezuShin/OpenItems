package su.nezushin.openitems.rp.textures;

import java.util.Map;

public record ResolvedBlockTextures(String pathAndName, TextureLayout layout, Map<String, String> replacements) {
}

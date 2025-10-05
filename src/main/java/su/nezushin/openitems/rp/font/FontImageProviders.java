package su.nezushin.openitems.rp.font;

import java.util.List;
/**
 * Used for resource pack json serialization
 */
public class FontImageProviders {

    private List<FontImage> providers;

    public FontImageProviders(List<FontImage> providers) {
        this.providers = providers;
    }

    public List<FontImage> getProviders() {
        return providers;
    }
}

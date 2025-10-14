package su.nezushin.openitems.rp.font;

import java.util.HashMap;
import java.util.Map;
/**
 * Used for resource pack json serialization
 */
public class SpaceFontImage extends FontImage{


    private Map<String, Integer> advances = new HashMap<>();

    public SpaceFontImage() {
        super("space");
    }


    public Map<String, Integer> getAdvances() {
        return advances;
    }
}

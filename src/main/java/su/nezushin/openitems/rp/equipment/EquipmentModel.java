package su.nezushin.openitems.rp.equipment;

import com.google.common.collect.Lists;
import su.nezushin.openitems.rp.NamespacedSectionBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentModel {

    private Map<String, List<EquipmentModelTexture>> layers = new HashMap<>();

    public EquipmentModel(String path, List<String> layers) {
        layers.forEach(i -> {
            this.layers.put(i, Lists.newArrayList(new EquipmentModelTexture(path)));
        });
    }
}

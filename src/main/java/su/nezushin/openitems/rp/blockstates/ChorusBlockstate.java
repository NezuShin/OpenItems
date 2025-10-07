package su.nezushin.openitems.rp.blockstates;

import su.nezushin.openitems.blocks.types.CustomChorusModel;
import su.nezushin.openitems.blocks.types.CustomTripwireModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for resource pack json serialization
 */
public class ChorusBlockstate {

    private Map<String, BlockstateModel> variants = new HashMap<>();

    public ChorusBlockstate(Map<String, Integer> tripwireIds) {
        for (var i : tripwireIds.entrySet()) {
            variants.put(CustomChorusModel.toBlocksate(i.getValue()), new BlockstateModel(i.getKey()));
        }
    }

}

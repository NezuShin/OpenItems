package su.nezushin.openitems.rp.blockstates;

import su.nezushin.openitems.blocks.types.CustomTripwireModel;

import java.util.HashMap;
import java.util.Map;
/**
 * Used for resource pack json serialization
 */
public class TripwireBlockstate {

    private Map<String, BlockstateModel> variants = new HashMap<>();

    public TripwireBlockstate(Map<String, Integer> tripwireIds) {
        for (var i : tripwireIds.entrySet()) {
            variants.put(CustomTripwireModel.toBlocksate(i.getValue()), new BlockstateModel(i.getKey()));
        }
    }

}

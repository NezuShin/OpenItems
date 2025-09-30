package su.nezushin.openitems.blocks.blockstates;

import su.nezushin.openitems.blocks.types.CustomNoteblockModel;

import java.util.HashMap;
import java.util.Map;

public class NoteblockBlockstate {

    private Map<String, BlockstateModel> variants = new HashMap<>();

    public NoteblockBlockstate(Map<String, Integer> noteblockIds) {
        for (var i : noteblockIds.entrySet()) {
            variants.put(CustomNoteblockModel.toBlocksate(i.getValue()), new BlockstateModel(i.getKey()));
        }
    }

}

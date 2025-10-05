package su.nezushin.openitems.rp.cache;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.rp.blockstates.NoteblockBlockstate;
import su.nezushin.openitems.rp.blockstates.TripwireBlockstate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to store and assign new ids for blocks
 */
public class BlockIdCache extends JsonCache {

    private Map<String, Integer> noteblockIds = new HashMap<>();
    private Map<String, Integer> registeredNoteblockIds = new HashMap<>();

    private Map<String, Integer> tripwireIds = new HashMap<>();
    private Map<String, Integer> registeredTripwireIds = new HashMap<>();

    private int nextNoteblockId = 1;
    private int nextTripwireId = 1;


    public int getOrCreateNoteblockId(String name) {
        var id = noteblockIds.get(name);

        if (id == null) {
            id = nextNoteblockId++;
            noteblockIds.put(name, id);
        }

        return id;
    }

    public int getOrCreateTripwireId(String name) {
        var id = tripwireIds.get(name);

        if (id == null) {
            id = nextTripwireId++;
            tripwireIds.put(name, id);
        }

        return id;
    }

    public void build() throws IOException {
        var blockstatesDir = new File(OpenItems.getInstance().getDataFolder(), "build/assets/minecraft/blockstates");

        blockstatesDir.mkdirs();

        var noteblockIdCache = new File(blockstatesDir, "note_block.json");
        var tripwireIdCache = new File(blockstatesDir, "tripwire.json");

        Files.writeString(noteblockIdCache.toPath(), new Gson().toJson(new NoteblockBlockstate(this.noteblockIds)), Charsets.UTF_8);
        Files.writeString(tripwireIdCache.toPath(), new Gson().toJson(new TripwireBlockstate(this.tripwireIds)), Charsets.UTF_8);
    }

    public void cleanRegistered() {
        this.registeredNoteblockIds.clear();
        this.registeredTripwireIds.clear();
    }

    @Override
    protected String getName() {
        return "block-id-cache";
    }

    public Map<String, Integer> getNoteblockIds() {
        return noteblockIds;
    }

    public Map<String, Integer> getRegisteredNoteblockIds() {
        return registeredNoteblockIds;
    }

    public Map<String, Integer> getRegisteredTripwireIds() {
        return registeredTripwireIds;
    }
}

package su.nezushin.openitems.rp;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.blockstates.NoteblockBlockstate;
import su.nezushin.openitems.blocks.blockstates.TripwireBlockstate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class BlockIdCache {

    private Map<String, Integer> noteblockIds = new HashMap<>();
    private Map<String, Integer> registredNoteblockIds = new HashMap<>();

    private Map<String, Integer> tripwireIds = new HashMap<>();
    private Map<String, Integer> registredTripwireIds = new HashMap<>();

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

    public void cleanRegistred() {
        this.registredNoteblockIds.clear();
        this.registredTripwireIds.clear();
    }

    public void save() throws IOException {
        var blockIdCache = new File(OpenItems.getInstance().getDataFolder(), "block-id-cache.json");

        Files.writeString(blockIdCache.toPath(), new Gson().toJson(this), Charsets.UTF_8);
    }

    public void load() throws IOException {
        var blockIdCache = new File(OpenItems.getInstance().getDataFolder(), "block-id-cache.json");

        if (!blockIdCache.exists())
            return;

        new Gson().fromJson(Files.readString(blockIdCache.toPath(), Charsets.UTF_8), BlockIdCache.class);
    }

    public Map<String, Integer> getNoteblockIds() {
        return noteblockIds;
    }

    public Map<String, Integer> getRegistredNoteblockIds() {
        return registredNoteblockIds;
    }

    public Map<String, Integer> getRegistredTripwireIds() {
        return registredTripwireIds;
    }
}

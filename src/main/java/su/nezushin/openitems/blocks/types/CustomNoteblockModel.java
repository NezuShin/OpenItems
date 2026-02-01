package su.nezushin.openitems.blocks.types;

import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;

import java.util.HashMap;
import java.util.Map;

public class CustomNoteblockModel implements CustomBlockModel {

    private static Map<Instrument, String> instrumentToBlockstate = new HashMap<>();

    static {
        instrumentToBlockstate.put(Instrument.BANJO, "banjo");
        instrumentToBlockstate.put(Instrument.BASS_DRUM, "basedrum");
        instrumentToBlockstate.put(Instrument.BASS_GUITAR, "bass");
        instrumentToBlockstate.put(Instrument.BELL, "bell");
        instrumentToBlockstate.put(Instrument.BIT, "bit");
        instrumentToBlockstate.put(Instrument.CHIME, "chime");
        instrumentToBlockstate.put(Instrument.COW_BELL, "cow_bell");
        instrumentToBlockstate.put(Instrument.CREEPER, "creeper");
        instrumentToBlockstate.put(Instrument.CUSTOM_HEAD, "custom_head");
        instrumentToBlockstate.put(Instrument.DIDGERIDOO, "didgeridoo");
        instrumentToBlockstate.put(Instrument.DRAGON, "dragon");
        instrumentToBlockstate.put(Instrument.FLUTE, "flute");
        instrumentToBlockstate.put(Instrument.PIANO, "harp");
        instrumentToBlockstate.put(Instrument.IRON_XYLOPHONE, "iron_xylophone");
        instrumentToBlockstate.put(Instrument.PIGLIN, "piglin");
        instrumentToBlockstate.put(Instrument.PLING, "pling");
        instrumentToBlockstate.put(Instrument.SKELETON, "skeleton");
        instrumentToBlockstate.put(Instrument.SNARE_DRUM, "snare");
        instrumentToBlockstate.put(Instrument.XYLOPHONE, "xylophone");
        instrumentToBlockstate.put(Instrument.ZOMBIE, "zombie");
        instrumentToBlockstate.put(Instrument.GUITAR, "guitar");
        instrumentToBlockstate.put(Instrument.STICKS, "hat");
    }

    private int id;

    private Map<BlockFace, Integer> faceMap = new HashMap<>();

    public CustomNoteblockModel(int id) {
        this.id = id;
    }

    @Override
    public void apply(Block b) {

        b.setType(Material.NOTE_BLOCK);

        if (b.getBlockData() instanceof NoteBlock nb) {
            setId(nb, id);
            b.setBlockData(nb);
            //b.getState().update(true, true);
        }
    }

    @Override
    public void apply(BlockData b) {
        if (!(b instanceof NoteBlock nb))
            return;

        setId(nb, id);
    }

    @Override
    public boolean isSimilar(Block b) {
        return b.getBlockData() instanceof NoteBlock nb && getId(nb) == this.id;
    }

    @Override
    public boolean applyOnPhysics() {
        return true;
    }

    public static void setId(NoteBlock nb, int id) {
        int powered = id & 0b1;

        int temp = (id >> 1);

        int note = temp % 25;

        int instrument = temp / 25;

        nb.setNote(new Note(note));
        nb.setInstrument(Instrument.getByType((byte) instrument));
        nb.setPowered(powered == 1);
    }

    public static void setDefaultId(NoteBlock nb){
        setId(nb, 0);
    }

    public static int getId(NoteBlock nb) {
        return getId(nb.getInstrument(), nb.getNote().getId(), nb.isPowered());
    }

    public static int getId(Instrument instrument, int note, boolean powered) {
        return ((((int) instrument.getType()) * (25 + note)) << 1) | (powered ? 1 : 0);
    }

    public static String toBlocksate(int id) {
        int powered = id & 0b1;

        int temp = (id >> 1);

        int note = temp % 25;

        int instrument = temp / 25;
        return "instrument=" + instrumentToBlockstate.get(Instrument.getByType((byte) instrument)) + ",note=" + note + ",powered=" + (powered == 1);
    }
}

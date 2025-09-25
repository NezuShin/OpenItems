package su.nezushin.openitems.blocks.types;

import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;

import java.util.Arrays;

public class CustomNoteblockType implements CustomBlockType {

    private int id;

    public CustomNoteblockType(int id) {
        this.id = id;
    }

    @Override
    public void apply(Block b) {

        b.setType(Material.NOTE_BLOCK);

        if (b.getBlockData() instanceof NoteBlock nb) {
            setId(nb, id);
            b.setBlockData(nb);
            b.getState().update(true, true);
        }
    }

    @Override
    public boolean isSimilar(Block b) {
        return b.getBlockData() instanceof NoteBlock nb && getId(nb) == this.id;
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

    public static int getId(NoteBlock nb) {
        return getId(nb.getInstrument(), nb.getNote().getId(), nb.isPowered());
    }

    public static int getId(Instrument instrument, int note, boolean powered) {
        return ((((int) instrument.getType()) * (25 + note)) << 1) | (powered ? 1 : 0);
    }
}

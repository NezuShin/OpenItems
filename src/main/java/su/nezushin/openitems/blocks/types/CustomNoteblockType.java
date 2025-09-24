package su.nezushin.openitems.blocks.types;

import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;

import java.util.Arrays;

public class CustomNoteblockType implements CustomBlockType {

    private int id;

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
        int note = (id >> 1) & 0b11;
        int instrument = (id >> 3);

        nb.setNote(new Note(note));
        nb.setInstrument(Instrument.values()[instrument]);
        nb.setPowered(powered == 1);
    }

    public static int getId(NoteBlock nb) {
        int instrument = Arrays.asList(Instrument.values()).indexOf(nb.getInstrument());
        int note = nb.getNote().getId();
        int powered = nb.isPowered() ? 1 : 0;

        return (instrument << 3) | (note << 1) | powered;
    }
}

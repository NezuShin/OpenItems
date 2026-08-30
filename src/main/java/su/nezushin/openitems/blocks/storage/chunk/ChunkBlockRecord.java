package su.nezushin.openitems.blocks.storage.chunk;

import com.google.gson.annotations.SerializedName;

final class ChunkBlockRecord {

    @SerializedName("i")
    int paletteIndex;

    @SerializedName("p")
    int packedPosition;

    @SerializedName("m")
    String extrasYaml;

    ChunkBlockRecord() {
    }

    ChunkBlockRecord(int paletteIndex, int packedPosition, String extrasYaml) {
        this.paletteIndex = paletteIndex;
        this.packedPosition = packedPosition;
        this.extrasYaml = extrasYaml;
    }
}

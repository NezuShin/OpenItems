package su.nezushin.openitems.blocks.storage.chunk;

import org.bukkit.Chunk;

final class ChunkBlockPositionCodec {

    private ChunkBlockPositionCodec() {
    }

    static int pack(Chunk chunk, int x, int y, int z) {
        int localX = x & 15;
        int localZ = z & 15;
        int localY = y - chunk.getWorld().getMinHeight();
        return (localY << 8) | (localZ << 4) | localX;
    }

    static int[] unpack(Chunk chunk, int packed) {
        int localX = packed & 15;
        int localZ = (packed >> 4) & 15;
        int localY = packed >>> 8;
        int worldX = chunk.getX() * 16 + localX;
        int worldZ = chunk.getZ() * 16 + localZ;
        int worldY = localY + chunk.getWorld().getMinHeight();
        return new int[]{worldX, worldY, worldZ};
    }
}

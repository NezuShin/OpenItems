package su.nezushin.openitems.blocks;

public class BlockStore {
    private String id;
    private int x, y, z;

    public BlockStore(String id, int x, int y, int z) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}

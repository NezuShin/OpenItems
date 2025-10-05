package su.nezushin.openitems.rp.blockstates;

/**
 * Used for resource pack json serialization
 */
public class BlockstateModel {

    private String model;

    public BlockstateModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}

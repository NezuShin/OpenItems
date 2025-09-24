package su.nezushin.openitems.blocks;

import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import su.nezushin.openitems.OpenItems;

public class CustomBlock {


    public static String getId(Block block) {
        var metadata = block.getMetadata("ns:custom_block_id");

        if (metadata.isEmpty())
            return null;

        return metadata.getFirst().asString();
    }

    public static void setId(Block block, String id) {
        block.setMetadata("ns:custom_block_id", new FixedMetadataValue(OpenItems.getInstance(), id));
    }

}

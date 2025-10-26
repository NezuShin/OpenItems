package su.nezushin.openitems.hooks;

import com.magmaguy.resourcepackmanager.api.ResourcePackManagerAPI;
import su.nezushin.openitems.OpenItems;

import java.io.File;

public class ResourcePackManagerHook {

    public void register() {
        ResourcePackManagerAPI.registerLocalResourcePack("OpenItems", "OpenItems/build",
                false,
                false,
                false,
                "oi build"
        );
    }

    public void build(){
        ResourcePackManagerAPI.reloadResourcePack();
    }
}

package su.nezushin.openitems.hooks;

public class RoseResourcepackHook {

    public void register() {
    }



    public void build() {
        //Ugly way to reload RoseRP. Needed to prevent adding dependency as file to build.gradle
        //TODO: Ask its developer to host maven repo
        try {
            //RoseRP.getInstance().reloadPlugin();
            var clazz = Class.forName("me.emsockz.roserp.RoseRP");
            var instance = clazz.getDeclaredMethod("getInstance")
                    .invoke(null);
            clazz.getDeclaredMethod("reloadPlugin").invoke(instance);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

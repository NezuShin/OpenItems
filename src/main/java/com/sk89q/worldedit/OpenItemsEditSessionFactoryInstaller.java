package com.sk89q.worldedit;

import java.lang.reflect.Field;

/**
 * Wraps WorldEdit's {@link EditSessionFactory} so created sessions use the OpenItems world wrapper.
 */
public final class OpenItemsEditSessionFactoryInstaller {

    private OpenItemsEditSessionFactoryInstaller() {
    }

    public static void install() {
        try {
            Field factoryField = WorldEdit.class.getDeclaredField("editSessionFactory");
            factoryField.setAccessible(true);
            WorldEdit worldEdit = WorldEdit.getInstance();
            EditSessionFactory current = (EditSessionFactory) factoryField.get(worldEdit);
            if (current instanceof OpenItemsEditSessionFactory)
                return;
            factoryField.set(worldEdit, new OpenItemsEditSessionFactory(current));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to install OpenItemsEditSessionFactory", e);
        }
    }

    public static void uninstall() {
        try {
            Field factoryField = WorldEdit.class.getDeclaredField("editSessionFactory");
            factoryField.setAccessible(true);
            WorldEdit worldEdit = WorldEdit.getInstance();
            EditSessionFactory current = (EditSessionFactory) factoryField.get(worldEdit);
            if (!(current instanceof OpenItemsEditSessionFactory wrapped))
                return;
            factoryField.set(worldEdit, wrapped.getDelegate());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to uninstall OpenItemsEditSessionFactory", e);
        }
    }
}

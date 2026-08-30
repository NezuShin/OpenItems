package com.sk89q.worldedit;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Copies an existing {@link LocalSession} into {@link OpenItemsLocalSession}.
 */
public final class OpenItemsLocalSessionUpgrade {

    private OpenItemsLocalSessionUpgrade() {
    }

    public static OpenItemsLocalSession upgrade(LocalSession from) {
        OpenItemsLocalSession to = new OpenItemsLocalSession();
        copyFields(from, to);
        return to;
    }

    private static void copyFields(Object from, Object to) {
        List<Class<?>> types = new ArrayList<>();
        for (Class<?> type = from.getClass(); type != null; type = type.getSuperclass())
            types.add(type);

        for (int i = types.size() - 1; i >= 0; i--) {
            for (Field field : types.get(i).getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()))
                    continue;
                try {
                    field.setAccessible(true);
                    field.set(to, field.get(from));
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to copy LocalSession field: " + field.getName(), e);
                }
            }
        }
    }
}

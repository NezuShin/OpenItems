package com.sk89q.worldedit.session;

import com.sk89q.worldedit.WorldEdit;

import java.lang.reflect.Field;

/**
 * Replaces WorldEdit's {@link SessionManager} with {@link OpenItemsSessionManager}.
 */
public final class OpenItemsSessionManagerInstaller {

    public static void install() {
        WorldEdit worldEdit = WorldEdit.getInstance();
        try {
            Field sessionsField = WorldEdit.class.getDeclaredField("sessions");
            sessionsField.setAccessible(true);
            SessionManager current = (SessionManager) sessionsField.get(worldEdit);
            if (current instanceof OpenItemsSessionManager)
                return;
            sessionsField.set(worldEdit, new OpenItemsSessionManager(worldEdit));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to install OpenItemsSessionManager", e);
        }
    }

    public static void uninstall() {
        WorldEdit worldEdit = WorldEdit.getInstance();
        try {
            Field sessionsField = WorldEdit.class.getDeclaredField("sessions");
            sessionsField.setAccessible(true);
            SessionManager current = (SessionManager) sessionsField.get(worldEdit);
            if (!(current instanceof OpenItemsSessionManager))
                return;
            sessionsField.set(worldEdit, new SessionManager(worldEdit));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to uninstall OpenItemsSessionManager", e);
        }
    }
}

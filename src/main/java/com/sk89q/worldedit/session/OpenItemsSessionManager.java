package com.sk89q.worldedit.session;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.OpenItemsLocalSession;
import com.sk89q.worldedit.OpenItemsLocalSessionUpgrade;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.session.SessionOwner;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

/**
 * Wraps {@link SessionManager} so player sessions use {@link OpenItemsLocalSession}.
 */
public final class OpenItemsSessionManager extends SessionManager {

    public OpenItemsSessionManager(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public synchronized LocalSession get(SessionOwner owner) {
        LocalSession session = super.get(owner);
        if (session instanceof OpenItemsLocalSession)
            return session;

        OpenItemsLocalSession upgraded = OpenItemsLocalSessionUpgrade.upgrade(session);
        replaceStoredSession(owner.getSessionKey(), upgraded);
        return upgraded;
    }

    private void replaceStoredSession(com.sk89q.worldedit.session.SessionKey sessionKey, LocalSession upgraded) {
        try {
            Field sessionsField = SessionManager.class.getDeclaredField("sessions");
            sessionsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, Object> sessions = (Map<UUID, Object>) sessionsField.get(this);

            UUID key = getKey(sessionKey);
            Object holder = sessions.get(key);
            if (holder == null)
                return;

            Field sessionField = holder.getClass().getDeclaredField("session");
            sessionField.setAccessible(true);
            sessionField.set(holder, upgraded);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to upgrade WorldEdit LocalSession", e);
        }
    }
}

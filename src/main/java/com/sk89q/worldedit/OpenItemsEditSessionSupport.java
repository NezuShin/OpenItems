package com.sk89q.worldedit;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.world.World;
import su.nezushin.openitems.hooks.worldedit.OpenItemsWorldEditWorlds;
import su.nezushin.openitems.hooks.worldedit.WorldEditSupportState;

import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

/**
 * WorldEdit hooks that live in {@code com.sk89q.worldedit} to access package-private APIs.
 */
public final class OpenItemsEditSessionSupport {

    private static volatile BooleanSupplier extendedActive = () -> false;

    public static void setExtendedActive(BooleanSupplier extendedActiveSupplier) {
        extendedActive = extendedActiveSupplier;
    }

    /**
     * Wrap a world before passing it to {@link EditSessionBuilder} when extended vanilla WE support is active.
     *
     * <p>{@link EditSession#world} is final and cannot be replaced after construction.
     */
    public static World wrapWorld(World world) {
        if (!extendedActive.getAsBoolean())
            return world;
        return OpenItemsWorldEditWorlds.wrap(world, WorldEditSupportState::isBasicActive);
    }

    static void prepareEditingExtents(LocalSession session, EditSession editSession, Actor actor) {
        try {
            Method method = LocalSession.class.getDeclaredMethod(
                    "prepareEditingExtents", EditSession.class, Actor.class);
            method.setAccessible(true);
            method.invoke(session, editSession, actor);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to prepare WorldEdit editing extents", e);
        }
    }
}

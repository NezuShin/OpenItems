package com.sk89q.worldedit;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.world.World;
import su.nezushin.openitems.hooks.worldedit.OpenItemsWorldEditWorlds;

import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

/**
 * WorldEdit hooks that live in {@code com.sk89q.worldedit} to access package-private APIs.
 */
public final class OpenItemsEditSessionSupport {

    private static volatile BooleanSupplier active = () -> false;

    private OpenItemsEditSessionSupport() {
    }

    public static void setActive(BooleanSupplier activeSupplier) {
        active = activeSupplier;
    }

    /**
     * Wrap a world before passing it to {@link EditSessionBuilder}.
     *
     * <p>{@link EditSession#world} is final and cannot be replaced after construction.
     */
    public static World wrapWorld(World world) {
        if (!active.getAsBoolean())
            return world;
        return OpenItemsWorldEditWorlds.wrap(world, active);
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

package com.sk89q.worldedit;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.world.World;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ensures edit sessions created through {@link LocalSession} read custom blocks via the wrapped world.
 */
public final class OpenItemsLocalSession extends LocalSession {

    @Override
    public EditSession createEditSession(Actor actor) {
        checkNotNull(actor);

        World world = null;
        if (hasWorldOverride()) {
            world = getWorldOverride();
        } else if (actor instanceof Locatable locatable && locatable.getExtent() instanceof World actorWorld) {
            world = actorWorld;
        }

        world = OpenItemsEditSessionSupport.wrapWorld(world);

        EditSessionBuilder builder = WorldEdit.getInstance().newEditSessionBuilder()
                .world(world)
                .actor(actor)
                .maxBlocks(getBlockChangeLimit())
                .tracing(isTracingActions());
        if (actor.isPlayer() && actor instanceof Player player) {
            builder.blockBag(getBlockBag(player));
        }

        EditSession editSession = builder.build();
        Request.request().setEditSession(editSession);

        editSession.setMask(getMask());
        OpenItemsEditSessionSupport.prepareEditingExtents(this, editSession, actor);
        return editSession;
    }
}

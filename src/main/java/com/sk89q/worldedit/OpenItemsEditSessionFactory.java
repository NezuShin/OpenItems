package com.sk89q.worldedit;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.world.World;

/**
 * Delegates to WorldEdit's factory with the OpenItems world wrapper applied before session creation.
 */
public final class OpenItemsEditSessionFactory extends EditSessionFactory {

    private final EditSessionFactory delegate;

    public OpenItemsEditSessionFactory(EditSessionFactory delegate) {
        this.delegate = delegate;
    }

    EditSessionFactory getDelegate() {
        return delegate;
    }

    @Override
    public EditSession getEditSession(World world, int maxBlocks) {
        return delegate.getEditSession(OpenItemsEditSessionSupport.wrapWorld(world), maxBlocks);
    }

    @Override
    public EditSession getEditSession(World world, int maxBlocks, Actor actor) {
        return delegate.getEditSession(OpenItemsEditSessionSupport.wrapWorld(world), maxBlocks, actor);
    }

    @Override
    public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag) {
        return delegate.getEditSession(OpenItemsEditSessionSupport.wrapWorld(world), maxBlocks, blockBag);
    }

    @Override
    public EditSession getEditSession(World world, int maxBlocks, BlockBag blockBag, Actor actor) {
        return delegate.getEditSession(
                OpenItemsEditSessionSupport.wrapWorld(world), maxBlocks, blockBag, actor);
    }
}

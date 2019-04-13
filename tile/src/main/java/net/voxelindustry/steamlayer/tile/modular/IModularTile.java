package net.voxelindustry.steamlayer.tile.modular;

import net.voxelindustry.steamlayer.tile.descriptor.TileDescriptor;

import java.util.Collection;

public interface IModularTile
{
    Collection<TileModule> getModules();

    <T extends TileModule> T getModule(Class<T> moduleClass);

    <T extends TileModule> boolean hasModule(Class<T> moduleClass);

    TileDescriptor getDescriptor();
}

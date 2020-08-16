package net.voxelindustry.steamlayer.tile;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;

public class SteamLayerTile implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, serverWorld) ->
        {
            if (blockEntity instanceof ILoadable)
                ((ILoadable) blockEntity).serverLoad();
        });
    }
}

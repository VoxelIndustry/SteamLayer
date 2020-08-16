package net.voxelindustry.steamlayer.tile;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;

public class SteamLayerTileClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, serverWorld) ->
        {
            if (blockEntity instanceof ILoadable)
                ((ILoadable) blockEntity).clientLoad();
        });
    }
}

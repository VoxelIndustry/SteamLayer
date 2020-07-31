package net.voxelindustry.steamlayer.common;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.voxelindustry.steamlayer.common.tick.ClientTickCounter;

public class SteamLayerCommonClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ClientTickEvents.START_WORLD_TICK.register(world -> ClientTickCounter.instance().tick(world));
    }
}

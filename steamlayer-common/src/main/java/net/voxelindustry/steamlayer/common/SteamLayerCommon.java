package net.voxelindustry.steamlayer.common;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.voxelindustry.steamlayer.common.tick.ClientTickCounter;

public class SteamLayerCommon implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        ClientTickEvents.START_WORLD_TICK.register(world -> ClientTickCounter.instance().tick(world));
    }
}

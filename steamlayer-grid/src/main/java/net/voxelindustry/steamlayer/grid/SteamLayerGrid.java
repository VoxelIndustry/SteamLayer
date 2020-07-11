package net.voxelindustry.steamlayer.grid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class SteamLayerGrid implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> GridManager.onServerShutdown());
    }
}

package net.voxelindustry.steamlayer.tile;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.voxelindustry.steamlayer.tile.event.TileTickHandler;

public class SteamLayerTile implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        ServerTickEvents.END_SERVER_TICK.register(server -> TileTickHandler.tick());
    }
}

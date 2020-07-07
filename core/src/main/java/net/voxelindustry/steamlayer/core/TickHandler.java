package net.voxelindustry.steamlayer.core;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.voxelindustry.steamlayer.tile.event.TileTickHandler;

public class TickHandler
{
    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent e)
    {
        if (e.phase == TickEvent.Phase.START)
            return;

        TileTickHandler.tick();
    }
}

package net.voxelindustry.steamlayer.tile.event;

import com.google.common.collect.Queues;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.voxelindustry.steamlayer.tile.ILoadable;

import java.util.Queue;

public class TileTickHandler
{
    public static final Queue<ILoadable> loadables = Queues.newArrayDeque();

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent e)
    {
        if(e.phase == TickEvent.Phase.START)
            return;

        while (TileTickHandler.loadables.peek() != null)
            TileTickHandler.loadables.poll().load();
    }
}

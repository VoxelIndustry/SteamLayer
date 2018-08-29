package net.voxelindustry.steamlayer.tile.event;

import com.google.common.collect.Queues;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
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

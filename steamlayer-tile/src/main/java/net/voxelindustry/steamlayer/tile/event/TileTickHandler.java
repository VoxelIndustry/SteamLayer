package net.voxelindustry.steamlayer.tile.event;

import com.google.common.collect.Queues;
import net.voxelindustry.steamlayer.tile.ILoadable;

import java.util.Queue;

public class TileTickHandler
{
    public static final Queue<ILoadable> loadables = Queues.newArrayDeque();

    public static void tick()
    {
        while (TileTickHandler.loadables.peek() != null)
            TileTickHandler.loadables.poll().serverLoad();
    }
}

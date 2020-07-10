package net.voxelindustry.steamlayer.core;

import net.minecraft.world.World;

import java.util.WeakHashMap;

public class ClientTickCounter
{
    private static ClientTickCounter instance;

    public static ClientTickCounter instance()
    {
        if (instance == null)
            instance = new ClientTickCounter();
        return instance;
    }

    private WeakHashMap<World, Long> tickCounters;

    private ClientTickCounter()
    {

    }

    public void tick(World world)
    {
        tickCounters.compute(world, (existing, counter) -> counter + 1);
    }

    public long getCount(World world)
    {
        Long counter = tickCounters.get(world);

        if (counter == null)
            return 0;
        return counter;
    }
}

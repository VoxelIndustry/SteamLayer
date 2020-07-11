package net.voxelindustry.steamlayer.common.tick;

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

    private final WeakHashMap<World, Long> tickCounters;

    private ClientTickCounter()
    {
        tickCounters = new WeakHashMap<>();
    }

    public void tick(World world)
    {
        tickCounters.compute(world, (existing, counter) -> counter == null ? 0 : counter + 1);
    }

    public long getCount(World world)
    {
        Long counter = tickCounters.get(world);

        if (counter == null)
            return 0;
        return counter;
    }
}

package net.voxelindustry.steamlayer.common.tick;

import java.util.WeakHashMap;
import net.minecraft.world.level.Level;

public class ClientTickCounter
{
    private static ClientTickCounter instance;

    public static ClientTickCounter instance()
    {
        if (instance == null)
            instance = new ClientTickCounter();
        return instance;
    }

    private final WeakHashMap<Level, Long> tickCounters;

    private ClientTickCounter()
    {
        tickCounters = new WeakHashMap<>();
    }

    public void tick(Level world)
    {
        tickCounters.compute(world, (existing, counter) -> counter == null ? 0 : counter + 1);
    }

    public long getCount(Level world)
    {
        Long counter = tickCounters.get(world);

        if (counter == null)
            return 0;
        return counter;
    }
}

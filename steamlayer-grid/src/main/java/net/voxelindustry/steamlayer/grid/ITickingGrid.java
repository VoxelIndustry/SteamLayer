package net.voxelindustry.steamlayer.grid;

public interface ITickingGrid
{
    /**
     * @return amount of times this grid is ticked per second (20 ticks).
     * Min 1, Max 20
     */
    default int getTickRate()
    {
        return 20;
    }

    /**
     * @param tickIndex index of this tick in a cycle [0-19]
     */
    void tick(int tickIndex);
}

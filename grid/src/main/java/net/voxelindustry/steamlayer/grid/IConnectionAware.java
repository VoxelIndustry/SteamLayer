package net.voxelindustry.steamlayer.grid;

import net.minecraft.util.Direction;

public interface IConnectionAware
{
    void connectTrigger(Direction facing, CableGrid grid);

    void disconnectTrigger(Direction facing, CableGrid grid);
}

package net.voxelindustry.steamlayer.grid;

import net.minecraft.util.EnumFacing;

public interface IConnectionAware
{
    void connectTrigger(EnumFacing facing, CableGrid grid);

    void disconnectTrigger(EnumFacing facing, CableGrid grid);
}

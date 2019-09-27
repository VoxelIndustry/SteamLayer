package net.voxelindustry.steamlayer.network.action;

import net.minecraft.nbt.CompoundNBT;

@FunctionalInterface
public interface IActionCallback
{
    void call(CompoundNBT response);
}

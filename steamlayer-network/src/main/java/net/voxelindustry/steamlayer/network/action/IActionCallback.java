package net.voxelindustry.steamlayer.network.action;

import net.minecraft.nbt.CompoundTag;

@FunctionalInterface
public interface IActionCallback
{
    void call(CompoundTag response);
}

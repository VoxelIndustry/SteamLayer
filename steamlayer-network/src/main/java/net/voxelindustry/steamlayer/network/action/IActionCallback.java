package net.voxelindustry.steamlayer.network.action;

import net.minecraft.nbt.NbtCompound;

@FunctionalInterface
public interface IActionCallback
{
    void call(NbtCompound response);
}

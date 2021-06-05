package net.voxelindustry.steamlayer.tile.modular;

import net.minecraft.nbt.NbtCompound;

public interface ISerializableModule
{
    void fromNBT(NbtCompound tag);

    NbtCompound toNBT(NbtCompound tag);
}

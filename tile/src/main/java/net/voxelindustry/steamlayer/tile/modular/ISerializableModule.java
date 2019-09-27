package net.voxelindustry.steamlayer.tile.modular;

import net.minecraft.nbt.CompoundNBT;

public interface ISerializableModule
{
    void fromNBT(CompoundNBT tag);

    CompoundNBT toNBT(CompoundNBT tag);
}

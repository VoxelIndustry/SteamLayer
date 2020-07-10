package net.voxelindustry.steamlayer.tile.modular;

import net.minecraft.nbt.CompoundTag;

public interface ISerializableModule
{
    void fromNBT(CompoundTag tag);

    CompoundTag toNBT(CompoundTag tag);
}

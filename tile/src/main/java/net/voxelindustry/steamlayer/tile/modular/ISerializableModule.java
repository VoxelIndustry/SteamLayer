package net.voxelindustry.steamlayer.tile.modular;

import net.minecraft.nbt.NBTTagCompound;

public interface ISerializableModule
{
    void fromNBT(NBTTagCompound tag);

    NBTTagCompound toNBT(NBTTagCompound tag);
}

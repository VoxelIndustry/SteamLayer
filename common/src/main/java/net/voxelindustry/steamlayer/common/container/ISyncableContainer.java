package net.voxelindustry.steamlayer.common.container;

import net.minecraft.nbt.NBTTagCompound;

public interface ISyncableContainer
{
    void updateProperty(int id, NBTTagCompound property);
}

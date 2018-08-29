package net.voxelindustry.steamlayer.network.action;

import net.minecraft.nbt.NBTTagCompound;

public interface IActionReceiver
{
    void handle(ActionSender sender, String actionID, NBTTagCompound payload);
}

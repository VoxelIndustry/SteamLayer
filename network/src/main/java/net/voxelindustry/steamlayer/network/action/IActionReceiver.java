package net.voxelindustry.steamlayer.network.action;

import net.minecraft.nbt.CompoundNBT;

public interface IActionReceiver
{
    void handle(ActionSender sender, String actionID, CompoundNBT payload);
}

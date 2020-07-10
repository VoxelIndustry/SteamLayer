package net.voxelindustry.steamlayer.network.action;

import net.minecraft.nbt.CompoundTag;

public interface IActionReceiver
{
    void handle(ActionSender sender, String actionID, CompoundTag payload);
}

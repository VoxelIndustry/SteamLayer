package net.voxelindustry.steamlayer.network.action;

import net.minecraft.nbt.NbtCompound;

public interface IActionReceiver
{
    void handle(ActionSender sender, String actionID, NbtCompound payload);
}

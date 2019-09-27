package net.voxelindustry.steamlayer.container;

import net.minecraft.entity.player.PlayerEntity;

public interface IContainerProvider
{
    BuiltContainer createContainer(PlayerEntity player);
}

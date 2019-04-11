package net.voxelindustry.steamlayer.container;

import net.minecraft.entity.player.EntityPlayer;

@FunctionalInterface
public interface ContainerEvent
{
    void apply(BuiltContainer container, EntityPlayer player, boolean isClient);
}

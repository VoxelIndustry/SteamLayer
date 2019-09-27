package net.voxelindustry.steamlayer.container;

import net.minecraft.entity.player.PlayerEntity;

@FunctionalInterface
public interface ContainerEvent
{
    void apply(BuiltContainer container, PlayerEntity player, boolean isClient);
}

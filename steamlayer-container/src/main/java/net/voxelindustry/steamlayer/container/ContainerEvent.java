package net.voxelindustry.steamlayer.container;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface ContainerEvent
{
    void apply(BuiltContainer container, Player player, boolean isClient);
}

package net.voxelindustry.steamlayer.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface PacketConsumer
{
    void accept(RegistryFriendlyByteBuf buffer, Player player, BlockableEventLoop<? extends Runnable> threadExecutor);
}

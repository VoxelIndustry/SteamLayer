package net.voxelindustry.steamlayer.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface PacketConsumer
{
    void accept(FriendlyByteBuf buffer, Player player, BlockableEventLoop<? extends Runnable> threadExecutor);
}

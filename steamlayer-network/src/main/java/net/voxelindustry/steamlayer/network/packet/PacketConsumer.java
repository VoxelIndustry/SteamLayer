package net.voxelindustry.steamlayer.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.thread.ThreadExecutor;

@FunctionalInterface
public interface PacketConsumer
{
    void accept(PacketByteBuf buffer, PlayerEntity player, ThreadExecutor<? extends Runnable> threadExecutor);
}

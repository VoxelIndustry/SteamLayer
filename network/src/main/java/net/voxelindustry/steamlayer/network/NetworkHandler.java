package net.voxelindustry.steamlayer.network;


import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public class NetworkHandler
{
    public static void sendTileToPlayer(BlockEntity tile, ServerPlayerEntity player)
    {
        if (!tile.getWorld().isClient)
        {
            BlockEntityUpdateS2CPacket packet = tile.toUpdatePacket();

            if (packet == null)
                return;
            player.networkHandler.sendPacket(packet);
        }
    }

    public static void sendTileToRange(BlockEntityClientSerializable tile)
    {
        tile.sync();
    }
}

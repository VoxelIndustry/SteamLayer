package net.voxelindustry.steamlayer.network;


import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class NetworkHandler
{
    public static void sendTileToPlayer(BlockEntity tile, ServerPlayerEntity player)
    {
        if (!tile.getWorld().isClient)
        {
            var packet = tile.toUpdatePacket();

            if (packet == null)
                return;
            player.networkHandler.sendPacket(packet);
        }
    }
}

package net.voxelindustry.steamlayer.network;


import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NetworkHandler
{
    public static void sendTileToPlayer(BlockEntity tile, ServerPlayer player)
    {
        if (!tile.getLevel().isClientSide)
        {
            var packet = tile.getUpdatePacket();

            if (packet == null)
                return;
            player.connection.send(packet);
        }
    }
}

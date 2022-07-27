package net.voxelindustry.steamlayer.network.packet;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.voxelindustry.steamlayer.network.NetworkHandler;

@AllArgsConstructor
@NoArgsConstructor
public class TileSyncRequestPacket
{
    private String   dimensionKey;
    private BlockPos pos;

    public static TileSyncRequestPacket decode(PacketByteBuf buffer)
    {
        TileSyncRequestPacket packet = new TileSyncRequestPacket();
        packet.dimensionKey = buffer.readString();
        packet.pos = buffer.readBlockPos();

        return packet;
    }

    public static void encode(TileSyncRequestPacket packet, PacketByteBuf buffer)
    {
        buffer.writeString(packet.dimensionKey);
        buffer.writeBlockPos(packet.pos);
    }

    public static void handleServer(TileSyncRequestPacket packet, PlayerEntity player, ThreadExecutor<? extends Runnable> threadExecutor)
    {
        threadExecutor.execute(() ->
        {
            var chunkPos = new ChunkPos(packet.pos);

            if (player.getEntityWorld().getRegistryKey().getValue().equals(packet.dimensionKey)
                    && player.getEntityWorld().isChunkLoaded(chunkPos.x, chunkPos.z) &&
                    player.getEntityWorld().getBlockEntity(packet.pos) != null)
                NetworkHandler.sendTileToPlayer(player.getEntityWorld().getBlockEntity(packet.pos),
                        (ServerPlayerEntity) player);
        });
    }
}

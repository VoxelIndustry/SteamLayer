package net.voxelindustry.steamlayer.network.packet;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
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

    public static void handleServer(TileSyncRequestPacket packet, PacketContext context)
    {
        context.getTaskQueue().execute(() ->
        {
            ChunkPos chunkPos = new ChunkPos(packet.pos);

            if (context.getPlayer().getEntityWorld().getRegistryKey().getValue().equals(packet.dimensionKey)
                    && context.getPlayer().getEntityWorld().isChunkLoaded(chunkPos.x, chunkPos.z) &&
                    context.getPlayer().getEntityWorld().getBlockEntity(packet.pos) != null)
                NetworkHandler.sendTileToPlayer(context.getPlayer().getEntityWorld().getBlockEntity(packet.pos),
                        (ServerPlayerEntity) context.getPlayer());
        });
    }
}

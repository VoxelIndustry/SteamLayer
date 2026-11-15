package net.voxelindustry.steamlayer.network.packet;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.voxelindustry.steamlayer.network.NetworkHandler;

@AllArgsConstructor
@NoArgsConstructor
public class TileSyncRequestPacket
{
    private String   dimensionKey;
    private BlockPos pos;

    public static TileSyncRequestPacket decode(FriendlyByteBuf buffer)
    {
        TileSyncRequestPacket packet = new TileSyncRequestPacket();
        packet.dimensionKey = buffer.readUtf();
        packet.pos = buffer.readBlockPos();

        return packet;
    }

    public static void encode(TileSyncRequestPacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeUtf(packet.dimensionKey);
        buffer.writeBlockPos(packet.pos);
    }

    public static void handleServer(TileSyncRequestPacket packet, Player player, BlockableEventLoop<? extends Runnable> threadExecutor)
    {
        threadExecutor.execute(() ->
        {
            var chunkPos = ChunkPos.containing(packet.pos);

            if (player.level().dimension().identifier().toString().equals(packet.dimensionKey)
                    && player.level().hasChunk(chunkPos.x(), chunkPos.z()) &&
                    player.level().getBlockEntity(packet.pos) != null)
                NetworkHandler.sendTileToPlayer(player.level().getBlockEntity(packet.pos),
                        (ServerPlayer) player);
        });
    }
}

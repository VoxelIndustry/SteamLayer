package net.voxelindustry.steamlayer.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.thread.EffectiveSide;

public class NetworkHandler
{
    public static void sendTileToPlayer(TileEntity tile, ServerPlayerEntity player)
    {
        if (tile.getWorld() != null && !tile.getWorld().isRemote || EffectiveSide.get().isServer())
        {
            SUpdateTileEntityPacket packet = tile.getUpdatePacket();

            if (packet == null)
                return;
            player.connection.sendPacket(packet);
        }
    }

    public static void sendTileToRange(TileEntity tile)
    {
        if (tile.getWorld() != null && !tile.getWorld().isRemote || EffectiveSide.get().isServer())
        {
            final SUpdateTileEntityPacket packet = tile.getUpdatePacket();

            if (packet == null)
                return;

            IChunk chunk = tile.getWorld().getChunk(tile.getPos());
            if (((ServerWorld) tile.getWorld()).getChunkProvider().isChunkLoaded(chunk.getPos()))
                ((ServerWorld) tile.getWorld()).getChunkProvider().chunkManager.getTrackingPlayers(chunk.getPos(), false).forEach(player -> player.connection.sendPacket(packet));
        }
    }
}

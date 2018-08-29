package net.voxelindustry.steamlayer.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class NetworkHandler
{
    public static void sendTileToPlayer(TileEntity tile, EntityPlayerMP player)
    {
        if (tile.getWorld() != null && !tile.getWorld().isRemote || FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            SPacketUpdateTileEntity packet = tile.getUpdatePacket();

            if (packet == null)
                return;
            player.connection.sendPacket(packet);
        }
    }

    public static void sendTileToRange(TileEntity tile)
    {
        if (tile.getWorld() != null && !tile.getWorld().isRemote || FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            final SPacketUpdateTileEntity packet = tile.getUpdatePacket();

            if (packet == null)
                return;

            Chunk chunk = tile.getWorld().getChunkFromBlockCoords(tile.getPos());
            if (((WorldServer) tile.getWorld()).getPlayerChunkMap().contains(chunk.x, chunk.z))
            {
                for (EntityPlayerMP player : tile.getWorld().getPlayers(EntityPlayerMP.class, entityPlayerMP -> true))
                {
                    if (((WorldServer) tile.getWorld()).getPlayerChunkMap().isPlayerWatchingChunk(player, chunk.x,
                            chunk.z))
                        player.connection.sendPacket(packet);
                }
            }
        }
    }
}

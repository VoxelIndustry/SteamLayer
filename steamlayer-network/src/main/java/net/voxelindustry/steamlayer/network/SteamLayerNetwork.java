package net.voxelindustry.steamlayer.network;

import net.fabricmc.api.ModInitializer;
import net.voxelindustry.steamlayer.network.packet.PacketHandler;
import net.voxelindustry.steamlayer.network.packet.ServerActionHolderPacket;
import net.voxelindustry.steamlayer.network.packet.ServerGenericPacket;
import net.voxelindustry.steamlayer.network.packet.TileSyncRequestPacket;
import net.voxelindustry.steamlayer.network.tilesync.PartialTileSyncPacket;
import net.voxelindustry.steamlayer.network.tilesync.PartialTileSyncRequestPacket;

import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.*;

public class SteamLayerNetwork implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        SteamLayerPacketHandler.registerServerBoundHandler(GENERIC_PACKET,
                (buffer, player, threadExecutor) -> new ServerGenericPacket().decode(buffer).handleServer(player, threadExecutor));

        SteamLayerPacketHandler.registerServerBoundHandler(SERVER_ACTION_HOLDER,
                (buffer, player, threadExecutor) -> ServerActionHolderPacket.handleServer(ServerActionHolderPacket.decode(buffer), player, threadExecutor));

        SteamLayerPacketHandler.registerServerBoundHandler(TILE_SYNC_REQUEST,
                (buffer, player, threadExecutor) -> TileSyncRequestPacket.handleServer(TileSyncRequestPacket.decode(buffer), player, threadExecutor));

        PacketHandler.getInstance().register(PartialTileSyncPacket.class);
        PacketHandler.getInstance().register(PartialTileSyncRequestPacket.class);
    }
}

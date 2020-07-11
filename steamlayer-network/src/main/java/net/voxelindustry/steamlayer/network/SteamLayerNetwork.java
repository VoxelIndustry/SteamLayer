package net.voxelindustry.steamlayer.network;

import net.fabricmc.api.ModInitializer;
import net.voxelindustry.steamlayer.network.packet.ClientActionHolderPacket;
import net.voxelindustry.steamlayer.network.packet.GenericPacket;
import net.voxelindustry.steamlayer.network.packet.ServerActionHolderPacket;
import net.voxelindustry.steamlayer.network.packet.TileSyncRequestPacket;

import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.*;

public class SteamLayerNetwork implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        SteamLayerPacketHandler.registerServerBoundHandler(GENERIC_PACKET,
                                                           (buffer, context) -> GenericPacket.handle(GenericPacket.decode(buffer), context));

        SteamLayerPacketHandler.registerClientBoundHandler(GENERIC_PACKET,
                                                           (buffer, context) -> GenericPacket.handle(GenericPacket.decode(buffer), context));

        SteamLayerPacketHandler.registerServerBoundHandler(SERVER_ACTION_HOLDER,
                                                           (buffer, context) -> ServerActionHolderPacket.handle(ServerActionHolderPacket.decode(buffer), context));

        SteamLayerPacketHandler.registerClientBoundHandler(CLIENT_ACTION_HOLDER,
                                                           (buffer, context) -> ClientActionHolderPacket.handle(ClientActionHolderPacket.decode(buffer), context));

        SteamLayerPacketHandler.registerServerBoundHandler(TILE_SYNC_REQUEST,
                                                           (buffer, context) -> TileSyncRequestPacket.handle(TileSyncRequestPacket.decode(buffer), context));
    }
}

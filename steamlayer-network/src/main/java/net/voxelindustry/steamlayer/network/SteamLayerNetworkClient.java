package net.voxelindustry.steamlayer.network;

import net.fabricmc.api.ClientModInitializer;
import net.voxelindustry.steamlayer.network.packet.ClientActionHolderPacket;
import net.voxelindustry.steamlayer.network.packet.GenericPacket;

import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.CLIENT_ACTION_HOLDER;
import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.GENERIC_PACKET;

public class SteamLayerNetworkClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        SteamLayerPacketHandler.registerClientBoundHandler(GENERIC_PACKET,
                (buffer, context) -> GenericPacket.handle(GenericPacket.decode(buffer), context));

        SteamLayerPacketHandler.registerClientBoundHandler(CLIENT_ACTION_HOLDER,
                (buffer, context) -> ClientActionHolderPacket.handle(ClientActionHolderPacket.decode(buffer), context));
    }
}

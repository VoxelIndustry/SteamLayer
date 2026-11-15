package net.voxelindustry.steamlayer.network;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.voxelindustry.steamlayer.network.packet.ClientActionHolderPacket;
import net.voxelindustry.steamlayer.network.packet.ClientGenericPacket;

import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.CLIENT_ACTION_HOLDER;
import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.GENERIC_PACKET;

public class SteamLayerNetworkClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        SteamLayerPacketHandlerClient.registerClientBoundHandler(GENERIC_PACKET,
                (buffer, player, threadExecutor) ->
                {
                    var message = new ClientGenericPacket().decode(buffer).getMessage();
                    threadExecutor.execute(() ->
                            message.handle(Minecraft.getInstance().player));
                });

        SteamLayerPacketHandlerClient.registerClientBoundHandler(CLIENT_ACTION_HOLDER,
                (buffer, player, threadExecutor) -> ClientActionHolderPacket.handleClient(ClientActionHolderPacket.decode(buffer), threadExecutor));
    }
}

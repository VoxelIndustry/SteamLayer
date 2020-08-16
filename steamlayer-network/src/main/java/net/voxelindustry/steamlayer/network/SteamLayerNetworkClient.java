package net.voxelindustry.steamlayer.network;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.voxelindustry.steamlayer.network.packet.ClientActionHolderPacket;
import net.voxelindustry.steamlayer.network.packet.ClientGenericPacket;
import net.voxelindustry.steamlayer.network.packet.Message;

import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.CLIENT_ACTION_HOLDER;
import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.GENERIC_PACKET;

public class SteamLayerNetworkClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        SteamLayerPacketHandler.registerClientBoundHandler(GENERIC_PACKET,
                (buffer, context) ->
                {
                    Message message = new ClientGenericPacket().decode(buffer).getMessage();
                    context.getTaskQueue().execute(() ->
                    {
                        message.handle(MinecraftClient.getInstance().player);
                    });
                });

        SteamLayerPacketHandler.registerClientBoundHandler(CLIENT_ACTION_HOLDER,
                (buffer, context) -> ClientActionHolderPacket.handleClient(ClientActionHolderPacket.decode(buffer), context));
    }
}

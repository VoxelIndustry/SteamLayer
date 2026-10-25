package net.voxelindustry.steamlayer.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.voxelindustry.steamlayer.network.packet.PacketConsumer;

@Environment(EnvType.CLIENT)
public class SteamLayerPacketHandlerClient
{
    public static void registerClientBoundHandler(Identifier identifier, PacketConsumer consumer)
    {
        ClientPlayNetworking.registerGlobalReceiver(identifier, (client, handler, packetByteBuf, responseSender) -> consumer.accept(packetByteBuf, client.player, client));
    }
}

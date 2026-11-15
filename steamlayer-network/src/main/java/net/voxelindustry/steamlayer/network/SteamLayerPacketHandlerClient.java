package net.voxelindustry.steamlayer.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.Identifier;
import net.voxelindustry.steamlayer.network.packet.PacketConsumer;

@Environment(EnvType.CLIENT)
public class SteamLayerPacketHandlerClient
{
    public static void registerClientBoundHandler(Identifier identifier, PacketConsumer consumer)
    {
        ClientPlayNetworking.registerGlobalReceiver(SteamLayerPacketHandler.getChannelType(identifier),
                (payload, context) -> consumer.accept(payload.read(context.player().registryAccess()), context.player(), context.client()));
    }
}

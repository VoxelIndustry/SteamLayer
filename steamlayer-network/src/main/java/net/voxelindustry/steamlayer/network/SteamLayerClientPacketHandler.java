package net.voxelindustry.steamlayer.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;

public class SteamLayerClientPacketHandler
{
    public static void sendToServer(Packet<?> packet)
    {
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }
}

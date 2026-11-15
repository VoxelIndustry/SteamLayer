package net.voxelindustry.steamlayer.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;

public class SteamLayerClientPacketHandler
{
    public static void sendToServer(Packet<?> packet)
    {
        Minecraft.getInstance().getConnection().send(packet);
    }
}

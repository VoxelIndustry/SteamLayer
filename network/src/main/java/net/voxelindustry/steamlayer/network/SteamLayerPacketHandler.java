package net.voxelindustry.steamlayer.network;

import net.minecraftforge.fml.network.simple.SimpleChannel;

public class SteamLayerPacketHandler
{
    public static SimpleChannel handler;

    public static SimpleChannel getHandler()
    {
        return handler;
    }
}

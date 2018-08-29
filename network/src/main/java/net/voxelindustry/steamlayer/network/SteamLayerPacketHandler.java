package net.voxelindustry.steamlayer.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;

public class SteamLayerPacketHandler
{
    public static final SimpleNetworkWrapper INSTANCE =
            NetworkRegistry.INSTANCE.newSimpleChannel(SteamLayerConstants.MODID);
}

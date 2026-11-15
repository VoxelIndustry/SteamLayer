package net.voxelindustry.steamlayer.container;

import net.fabricmc.api.ModInitializer;
import net.voxelindustry.steamlayer.container.sync.ContainerSyncPacket;
import net.voxelindustry.steamlayer.network.packet.PacketHandler;

public class SteamLayerContainer implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        PacketHandler.getInstance().register(ContainerSyncPacket.class);
    }
}

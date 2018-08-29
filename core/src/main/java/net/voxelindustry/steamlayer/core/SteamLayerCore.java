package net.voxelindustry.steamlayer.core;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;
import net.voxelindustry.steamlayer.network.packet.ClientActionHolderPacket;
import net.voxelindustry.steamlayer.network.packet.ContainerUpdatePacket;
import net.voxelindustry.steamlayer.network.packet.ServerActionHolderPacket;
import net.voxelindustry.steamlayer.network.packet.TileSyncRequestPacket;
import net.voxelindustry.steamlayer.tile.event.TileTickHandler;

@Mod(modid = SteamLayerConstants.MODID, name = SteamLayerConstants.MODNAME, version = SteamLayerConstants.VERSION)
public class SteamLayerCore
{
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        SteamLayerPacketHandler.INSTANCE.registerMessage(ContainerUpdatePacket.ContainerUpdatePacketHandler.class,
                ContainerUpdatePacket.class, 0, Side.CLIENT);

        SteamLayerPacketHandler.INSTANCE.registerMessage(ClientActionHolderPacket.ClientActionHolderPacketHandler.class,
                ClientActionHolderPacket.class, 1, Side.CLIENT);

        SteamLayerPacketHandler.INSTANCE.registerMessage(ServerActionHolderPacket.ServerActionHolderPacketHandler.class,
                ServerActionHolderPacket.class, 2, Side.SERVER);

        SteamLayerPacketHandler.INSTANCE.registerMessage(TileSyncRequestPacket.TileSyncRequestPacketHandler.class,
                TileSyncRequestPacket.class, 3, Side.SERVER);

        MinecraftForge.EVENT_BUS.register(new TileTickHandler());
    }
}

package net.voxelindustry.steamlayer.core;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;
import net.voxelindustry.steamlayer.container.sync.ContainerSyncPacket;
import net.voxelindustry.steamlayer.grid.GridManager;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;
import net.voxelindustry.steamlayer.network.packet.*;
import net.voxelindustry.steamlayer.tile.event.TileTickHandler;

@Mod(modid = SteamLayerConstants.MODID, name = SteamLayerConstants.MODNAME, version = SteamLayerConstants.VERSION)
public class SteamLayerCore
{
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        SteamLayerPacketHandler.INSTANCE.registerMessage(ClientActionHolderPacket.ClientActionHolderPacketHandler.class,
                ClientActionHolderPacket.class, 1, Side.CLIENT);

        SteamLayerPacketHandler.INSTANCE.registerMessage(ServerActionHolderPacket.ServerActionHolderPacketHandler.class,
                ServerActionHolderPacket.class, 2, Side.SERVER);

        SteamLayerPacketHandler.INSTANCE.registerMessage(TileSyncRequestPacket.TileSyncRequestPacketHandler.class,
                TileSyncRequestPacket.class, 3, Side.SERVER);

        SteamLayerPacketHandler.INSTANCE.registerMessage(GenericPacket.GenericClientPacketHandler.class,
                GenericPacket.class, 4, Side.CLIENT);

        SteamLayerPacketHandler.INSTANCE.registerMessage(GenericPacket.GenericServerPacketHandler.class,
                GenericPacket.class, 5, Side.SERVER);

        PacketHandler.getInstance().register(ContainerSyncPacket.class);

        MinecraftForge.EVENT_BUS.register(new TileTickHandler());
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent e)
    {
        GridManager.onServerShutdown();
    }
}

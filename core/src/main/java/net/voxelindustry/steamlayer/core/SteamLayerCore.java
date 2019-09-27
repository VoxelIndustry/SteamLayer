package net.voxelindustry.steamlayer.core;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;
import net.voxelindustry.steamlayer.container.sync.ContainerSyncPacket;
import net.voxelindustry.steamlayer.grid.GridManager;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;
import net.voxelindustry.steamlayer.network.packet.PacketHandler;
import net.voxelindustry.steamlayer.tile.event.TileTickHandler;

@Mod(SteamLayerConstants.MODID)
public class SteamLayerCore
{
    public SteamLayerCore()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent e)
    {
        SteamLayerPacketHandler.register();
        PacketHandler.getInstance().register(ContainerSyncPacket.class);

        MinecraftForge.EVENT_BUS.register(new TileTickHandler());
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent e)
    {
        GridManager.onServerShutdown();
    }
}

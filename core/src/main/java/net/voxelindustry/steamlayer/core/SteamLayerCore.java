package net.voxelindustry.steamlayer.core;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.voxelindustry.steamlayer.container.sync.ContainerSyncPacket;
import net.voxelindustry.steamlayer.grid.GridManager;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;
import net.voxelindustry.steamlayer.network.packet.ClientActionHolderPacket;
import net.voxelindustry.steamlayer.network.packet.GenericPacket;
import net.voxelindustry.steamlayer.network.packet.PacketHandler;
import net.voxelindustry.steamlayer.network.packet.ServerActionHolderPacket;
import net.voxelindustry.steamlayer.network.packet.TileSyncRequestPacket;

@Mod("steamlayer")
public class SteamLayerCore
{
    private static final String PROTOCOL_VERSION = Integer.toString(1);

    public SteamLayerCore()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent e)
    {
        SteamLayerPacketHandler.handler = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation("steamlayer", "main"))
                .networkProtocolVersion(() -> "1")
                .clientAcceptedVersions("1"::equals)
                .serverAcceptedVersions("1"::equals)
                .simpleChannel();

        registerPackets();
        PacketHandler.getInstance().register(ContainerSyncPacket.class);

        MinecraftForge.EVENT_BUS.register(new TickHandler());
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent e)
    {
        GridManager.onServerShutdown();
    }

    private void registerPackets()
    {
        SteamLayerPacketHandler.getHandler().registerMessage(0,
                GenericPacket.class,
                GenericPacket::encode,
                GenericPacket::decode,
                GenericPacket::handle);

        SteamLayerPacketHandler.getHandler().registerMessage(1,
                ServerActionHolderPacket.class,
                ServerActionHolderPacket::encode,
                ServerActionHolderPacket::decode,
                ServerActionHolderPacket::handle);

        SteamLayerPacketHandler.getHandler().registerMessage(2,
                ClientActionHolderPacket.class,
                ClientActionHolderPacket::encode,
                ClientActionHolderPacket::decode,
                ClientActionHolderPacket::handle);

        SteamLayerPacketHandler.getHandler().registerMessage(3,
                TileSyncRequestPacket.class,
                TileSyncRequestPacket::encode,
                TileSyncRequestPacket::decode,
                TileSyncRequestPacket::handle);
    }
}

package net.voxelindustry.steamlayer.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;
import net.voxelindustry.steamlayer.network.packet.ClientActionHolderPacket;
import net.voxelindustry.steamlayer.network.packet.GenericPacket;
import net.voxelindustry.steamlayer.network.packet.ServerActionHolderPacket;
import net.voxelindustry.steamlayer.network.packet.TileSyncRequestPacket;

public class SteamLayerPacketHandler
{
    private static final String PROTOCOL_VERSION = Integer.toString(1);

    private static SimpleChannel handler;

    public static SimpleChannel getHandler()
    {
        return handler;
    }

    public static void register()
    {
        handler = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(SteamLayerConstants.MODID, "main_channel"))
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();

        handler.registerMessage(0,
                GenericPacket.class,
                GenericPacket::encode,
                GenericPacket::decode,
                GenericPacket::handle);

        handler.registerMessage(1,
                ServerActionHolderPacket.class,
                ServerActionHolderPacket::encode,
                ServerActionHolderPacket::decode,
                ServerActionHolderPacket::handle);

        handler.registerMessage(2,
                ClientActionHolderPacket.class,
                ClientActionHolderPacket::encode,
                ClientActionHolderPacket::decode,
                ClientActionHolderPacket::handle);

        handler.registerMessage(3,
                TileSyncRequestPacket.class,
                TileSyncRequestPacket::encode,
                TileSyncRequestPacket::decode,
                TileSyncRequestPacket::handle);
    }
}

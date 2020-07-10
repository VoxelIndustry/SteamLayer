package net.voxelindustry.steamlayer.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SteamLayerPacketHandler
{
    public static final Identifier TILE_SYNC_REQUEST    = new Identifier(SteamLayerConstants.MODID, "tile_sync.request");
    public static final Identifier GENERIC_PACKET       = new Identifier(SteamLayerConstants.MODID, "genericpacket");
    public static final Identifier SERVER_ACTION_HOLDER = new Identifier(SteamLayerConstants.MODID, "server_action.holder");
    public static final Identifier CLIENT_ACTION_HOLDER = new Identifier(SteamLayerConstants.MODID, "client_action.holder");

    public static Packet<ServerPlayPacketListener> createServerBoundPacket(Identifier identifier, Consumer<PacketByteBuf> packetBufferConsumer)
    {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        packetBufferConsumer.accept(buf);
        return new CustomPayloadC2SPacket(identifier, buf);
    }


    public static void registerServerBoundHandler(Identifier identifier, BiConsumer<PacketByteBuf, PacketContext> consumer)
    {
        ServerSidePacketRegistry.INSTANCE.register(identifier, (packetContext, packetByteBuf) -> consumer.accept(packetByteBuf, packetContext));
    }

    public static Packet<ClientPlayPacketListener> createClientBoundPacket(Identifier identifier, Consumer<PacketByteBuf> packetBufferConsumer)
    {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        packetBufferConsumer.accept(buf);
        return new CustomPayloadS2CPacket(identifier, buf);
    }

    public static void registerClientBoundHandler(Identifier identifier, BiConsumer<PacketByteBuf, PacketContext> consumer)
    {
        ClientSidePacketRegistry.INSTANCE.register(identifier, (packetContext, packetByteBuf) -> consumer.accept(packetByteBuf, packetContext));
    }

    public static void sendToServer(Packet<?> packet)
    {
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
    }

    public static void sendToAll(Packet<?> packet, MinecraftServer server)
    {
        server.getPlayerManager().sendToAll(packet);
    }

    public static void sendToPlayer(Packet<?> packet, ServerPlayerEntity serverPlayerEntity)
    {
        serverPlayerEntity.networkHandler.sendPacket(packet);
    }

    public static void sendToWorld(Packet<?> packet, ServerWorld world)
    {
        world.getPlayers().forEach(serverPlayerEntity -> sendToPlayer(packet, serverPlayerEntity));
    }

}

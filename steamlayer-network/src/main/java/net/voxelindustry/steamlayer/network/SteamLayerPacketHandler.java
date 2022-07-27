package net.voxelindustry.steamlayer.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;
import net.voxelindustry.steamlayer.network.packet.PacketConsumer;

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


    public static void registerServerBoundHandler(Identifier identifier, PacketConsumer consumer)
    {
        ServerPlayNetworking.registerGlobalReceiver(identifier, (server, playerEntity, channelHandler, packetByteBuf, responseSender) -> consumer.accept(packetByteBuf, playerEntity, server));
    }

    public static Packet<ClientPlayPacketListener> createClientBoundPacket(Identifier identifier, Consumer<PacketByteBuf> packetBufferConsumer)
    {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        packetBufferConsumer.accept(buf);
        return new CustomPayloadS2CPacket(identifier, buf);
    }

    public static void registerClientBoundHandler(Identifier identifier, PacketConsumer consumer)
    {
        ClientPlayNetworking.registerGlobalReceiver(identifier, (client, handler, packetByteBuf, responseSender) -> consumer.accept(packetByteBuf, client.player, client));
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
        world.getPlayers()
                .forEach(serverPlayerEntity -> sendToPlayer(packet, serverPlayerEntity));
    }

    public static void sendToTracking(Packet<?> packet, BlockEntity tile)
    {
        PlayerLookup.tracking(tile)
                .forEach(player -> sendToPlayer(packet, player));
    }

    public static void sendToTracking(Packet<?> packet, Entity entity)
    {
        PlayerLookup.tracking(entity)
                .forEach(player -> sendToPlayer(packet, player));
    }

    public static void sendToTracking(Packet<?> packet, ServerWorld world, BlockPos pos)
    {
        PlayerLookup.tracking(world, pos)
                .forEach(player -> sendToPlayer(packet, player));
    }

    public static void sendToTracking(Packet<?> packet, ServerWorld world, ChunkPos pos)
    {
        PlayerLookup.tracking(world, pos)
                .forEach(player -> sendToPlayer(packet, player));
    }
}

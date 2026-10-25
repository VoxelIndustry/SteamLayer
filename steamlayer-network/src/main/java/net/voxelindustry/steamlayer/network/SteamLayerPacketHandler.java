package net.voxelindustry.steamlayer.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;
import net.voxelindustry.steamlayer.network.packet.PacketConsumer;

import java.util.function.Consumer;

public class SteamLayerPacketHandler
{
    public static final ResourceLocation TILE_SYNC_REQUEST    = new ResourceLocation(SteamLayerConstants.MODID, "tile_sync.request");
    public static final ResourceLocation GENERIC_PACKET       = new ResourceLocation(SteamLayerConstants.MODID, "genericpacket");
    public static final ResourceLocation SERVER_ACTION_HOLDER = new ResourceLocation(SteamLayerConstants.MODID, "server_action.holder");
    public static final ResourceLocation CLIENT_ACTION_HOLDER = new ResourceLocation(SteamLayerConstants.MODID, "client_action.holder");

    public static Packet<ServerGamePacketListener> createServerBoundPacket(ResourceLocation identifier, Consumer<FriendlyByteBuf> packetBufferConsumer)
    {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packetBufferConsumer.accept(buf);
        return new ServerboundCustomPayloadPacket(identifier, buf);
    }


    public static void registerServerBoundHandler(ResourceLocation identifier, PacketConsumer consumer)
    {
        ServerPlayNetworking.registerGlobalReceiver(identifier, (server, playerEntity, channelHandler, packetByteBuf, responseSender) -> consumer.accept(packetByteBuf, playerEntity, server));
    }

    public static Packet<ClientGamePacketListener> createClientBoundPacket(ResourceLocation identifier, Consumer<FriendlyByteBuf> packetBufferConsumer)
    {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packetBufferConsumer.accept(buf);
        return new ClientboundCustomPayloadPacket(identifier, buf);
    }

    public static void sendToAll(Packet<?> packet, MinecraftServer server)
    {
        server.getPlayerList().broadcastAll(packet);
    }

    public static void sendToPlayer(Packet<?> packet, ServerPlayer serverPlayerEntity)
    {
        serverPlayerEntity.connection.send(packet);
    }

    public static void sendToWorld(Packet<?> packet, ServerLevel world)
    {
        world.players()
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

    public static void sendToTracking(Packet<?> packet, ServerLevel world, BlockPos pos)
    {
        PlayerLookup.tracking(world, pos)
                .forEach(player -> sendToPlayer(packet, player));
    }

    public static void sendToTracking(Packet<?> packet, ServerLevel world, ChunkPos pos)
    {
        PlayerLookup.tracking(world, pos)
                .forEach(player -> sendToPlayer(packet, player));
    }
}

package net.voxelindustry.steamlayer.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;
import net.voxelindustry.steamlayer.network.packet.PacketConsumer;
import net.voxelindustry.steamlayer.network.packet.SteamLayerPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SteamLayerPacketHandler
{
    private static final Map<Identifier, Type<SteamLayerPayload>> CHANNELS = new HashMap<>();

    public static final Identifier TILE_SYNC_REQUEST    = registerChannel("tile_sync.request");
    public static final Identifier GENERIC_PACKET       = registerChannel("genericpacket");
    public static final Identifier SERVER_ACTION_HOLDER = registerChannel("server_action.holder");
    public static final Identifier CLIENT_ACTION_HOLDER = registerChannel("client_action.holder");

    private static Identifier registerChannel(String name)
    {
        var identifier = Identifier.fromNamespaceAndPath(SteamLayerConstants.MODID, name);
        var type = new Type<SteamLayerPayload>(identifier);
        var codec = SteamLayerPayload.codec(type);

        PayloadTypeRegistry.serverboundPlay().register(type, codec);
        PayloadTypeRegistry.clientboundPlay().register(type, codec);
        CHANNELS.put(identifier, type);
        return identifier;
    }

    public static Type<SteamLayerPayload> getChannelType(Identifier identifier)
    {
        var type = CHANNELS.get(identifier);
        if (type == null)
            throw new IllegalArgumentException("Unknown SteamLayer channel " + identifier);
        return type;
    }

    public static Packet<?> createServerBoundPacket(Identifier identifier, Consumer<RegistryFriendlyByteBuf> packetBufferConsumer)
    {
        return new ServerboundCustomPayloadPacket(SteamLayerPayload.outgoing(getChannelType(identifier), packetBufferConsumer));
    }

    public static void registerServerBoundHandler(Identifier identifier, PacketConsumer consumer)
    {
        ServerPlayNetworking.registerGlobalReceiver(getChannelType(identifier),
                (payload, context) -> consumer.accept(payload.read(context.player().registryAccess()), context.player(), context.server()));
    }

    public static Packet<?> createClientBoundPacket(Identifier identifier, Consumer<RegistryFriendlyByteBuf> packetBufferConsumer)
    {
        return new ClientboundCustomPayloadPacket(SteamLayerPayload.outgoing(getChannelType(identifier), packetBufferConsumer));
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

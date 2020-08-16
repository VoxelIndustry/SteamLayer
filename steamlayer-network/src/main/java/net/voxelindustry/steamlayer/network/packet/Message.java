package net.voxelindustry.steamlayer.network.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.voxelindustry.steamlayer.network.SteamLayerClientPacketHandler;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;

public abstract class Message
{
    public abstract void read(PacketByteBuf buf);

    public abstract void write(PacketByteBuf buf);

    public abstract void handle(PlayerEntity player);

    public void sendTo(ServerPlayerEntity player)
    {
        SteamLayerPacketHandler.sendToPlayer(
                SteamLayerPacketHandler.createClientBoundPacket(SteamLayerPacketHandler.GENERIC_PACKET,
                        buffer -> new ClientGenericPacket(this).encode(buffer)),
                player);
    }

    @Environment(EnvType.CLIENT)
    public void sendToServer()
    {
        SteamLayerClientPacketHandler.sendToServer(SteamLayerPacketHandler.createServerBoundPacket(SteamLayerPacketHandler.GENERIC_PACKET,
                buffer -> new ServerGenericPacket(this).encode(buffer)));
    }

    public void sendToAll(MinecraftServer server)
    {
        SteamLayerPacketHandler.sendToAll(SteamLayerPacketHandler.createClientBoundPacket(SteamLayerPacketHandler.GENERIC_PACKET,
                buffer -> new ClientGenericPacket(this).encode(buffer)), server);
    }

    public void sendToDimension(ServerWorld world)
    {
        SteamLayerPacketHandler.sendToWorld(SteamLayerPacketHandler.createClientBoundPacket(SteamLayerPacketHandler.GENERIC_PACKET,
                buffer -> new ClientGenericPacket(this).encode(buffer)), world);
    }

    public void sendToTracking(Entity entity)
    {
        SteamLayerPacketHandler.sendToTracking(SteamLayerPacketHandler.createClientBoundPacket(SteamLayerPacketHandler.GENERIC_PACKET,
                buffer -> new ClientGenericPacket(this).encode(buffer)), entity);
    }

    public void sendToTracking(BlockEntity tile)
    {
        SteamLayerPacketHandler.sendToTracking(SteamLayerPacketHandler.createClientBoundPacket(SteamLayerPacketHandler.GENERIC_PACKET,
                buffer -> new ClientGenericPacket(this).encode(buffer)), tile);
    }

    public void sendToTracking(World world, BlockPos pos)
    {
        SteamLayerPacketHandler.sendToTracking(SteamLayerPacketHandler.createClientBoundPacket(SteamLayerPacketHandler.GENERIC_PACKET,
                buffer -> new ClientGenericPacket(this).encode(buffer)), world, pos);
    }

    public void sendToTracking(World world, ChunkPos pos)
    {
        SteamLayerPacketHandler.sendToTracking(SteamLayerPacketHandler.createClientBoundPacket(SteamLayerPacketHandler.GENERIC_PACKET,
                buffer -> new ClientGenericPacket(this).encode(buffer)), world, pos);
    }
}

package net.voxelindustry.steamlayer.network.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.voxelindustry.steamlayer.network.SteamLayerClientPacketHandler;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;

public abstract class Message
{
    public abstract void read(RegistryFriendlyByteBuf buf);

    public abstract void write(RegistryFriendlyByteBuf buf);

    public abstract void handle(Player player);

    public void sendTo(ServerPlayer player)
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

    public void sendToDimension(ServerLevel world)
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

    public void sendToTracking(ServerLevel world, BlockPos pos)
    {
        SteamLayerPacketHandler.sendToTracking(SteamLayerPacketHandler.createClientBoundPacket(SteamLayerPacketHandler.GENERIC_PACKET,
                buffer -> new ClientGenericPacket(this).encode(buffer)), world, pos);
    }

    public void sendToTracking(ServerLevel world, ChunkPos pos)
    {
        SteamLayerPacketHandler.sendToTracking(SteamLayerPacketHandler.createClientBoundPacket(SteamLayerPacketHandler.GENERIC_PACKET,
                buffer -> new ClientGenericPacket(this).encode(buffer)), world, pos);
    }
}

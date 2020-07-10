package net.voxelindustry.steamlayer.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
                        buffer -> GenericPacket.encode(new GenericPacket(this), buffer)),
                player);
    }

    public void sendToServer()
    {
        SteamLayerPacketHandler.sendToServer(SteamLayerPacketHandler.createServerBoundPacket(SteamLayerPacketHandler.GENERIC_PACKET,
                buffer -> GenericPacket.encode(new GenericPacket(this), buffer)));
    }

    public void sendToAll(MinecraftServer server)
    {
        SteamLayerPacketHandler.sendToAll(SteamLayerPacketHandler.createClientBoundPacket(SteamLayerPacketHandler.GENERIC_PACKET,
                buffer -> GenericPacket.encode(new GenericPacket(this), buffer)), server);
    }

    public void sendToDimension(ServerWorld world)
    {
        SteamLayerPacketHandler.sendToWorld(SteamLayerPacketHandler.createClientBoundPacket(SteamLayerPacketHandler.GENERIC_PACKET,
                buffer -> GenericPacket.encode(new GenericPacket(this), buffer)), world);
    }
}

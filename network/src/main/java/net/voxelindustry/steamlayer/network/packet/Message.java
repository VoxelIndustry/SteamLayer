package net.voxelindustry.steamlayer.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;

import static net.minecraftforge.fml.network.PacketDistributor.*;

public abstract class Message
{
    public abstract void read(ByteBuf buf);

    public abstract void write(ByteBuf buf);

    public abstract void handle(PlayerEntity player);

    public void sendTo(ServerPlayerEntity player)
    {
        SteamLayerPacketHandler.getHandler().send(PLAYER.with(() -> player), new GenericPacket(this));
    }

    public void sendToServer()
    {
        SteamLayerPacketHandler.getHandler().sendToServer(new GenericPacket(this));
    }

    public void sendToAll()
    {
        SteamLayerPacketHandler.getHandler().send(ALL.noArg(), new GenericPacket(this));
    }

    public void sendToDimension(DimensionType dimensionType)
    {
        SteamLayerPacketHandler.getHandler().send(DIMENSION.with(() -> dimensionType), new GenericPacket(this));
    }

    public void sendToWatchingChunk(Chunk chunk)
    {
        SteamLayerPacketHandler.getHandler().send(TRACKING_CHUNK.with(() -> chunk), new GenericPacket(this));
    }

    public void sendToAllAround(World world, BlockPos pos, int range)
    {
        SteamLayerPacketHandler.getHandler().send(NEAR
                        .with(() -> new TargetPoint(pos.getX(), pos.getY(), pos.getZ(), range, world.getDimension().getType())),
                new GenericPacket(this));
    }
}

package net.voxelindustry.steamlayer.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;

public abstract class Message
{
    public abstract void read(ByteBuf buf);

    public abstract void write(ByteBuf buf);

    public abstract void handle(EntityPlayer player);

    public void sendTo(EntityPlayerMP player)
    {
        SteamLayerPacketHandler.INSTANCE.sendTo(new GenericPacket(this), player);
    }

    public void sendToServer()
    {
        SteamLayerPacketHandler.INSTANCE.sendToServer(new GenericPacket(this));
    }

    public void sendToAll()
    {
        SteamLayerPacketHandler.INSTANCE.sendToAll(new GenericPacket(this));
    }

    public void sendToDimension(int dim)
    {
        SteamLayerPacketHandler.INSTANCE.sendToDimension(new GenericPacket(this), dim);
    }

    public void sendToAllAround(World world, BlockPos pos, int range)
    {
        SteamLayerPacketHandler.INSTANCE.sendToAllAround(new GenericPacket(this),
                new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(),
                        range));
    }
}

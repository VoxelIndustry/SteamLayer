package net.voxelindustry.steamlayer.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.voxelindustry.steamlayer.network.ByteBufHelper;
import net.voxelindustry.steamlayer.network.NetworkHandler;

import java.util.function.Supplier;

@AllArgsConstructor
@NoArgsConstructor
public class TileSyncRequestPacket
{
    private int      dimensionID;
    private BlockPos pos;

    public static TileSyncRequestPacket decode(ByteBuf buffer)
    {
        TileSyncRequestPacket packet = new TileSyncRequestPacket();
        packet.dimensionID = buffer.readInt();
        packet.pos = ByteBufHelper.readPos(buffer);

        return packet;
    }

    public static void encode(TileSyncRequestPacket packet, ByteBuf buffer)
    {
        buffer.writeInt(packet.dimensionID);
        ByteBufHelper.writePos(buffer, packet.pos);
    }

    public static void handle(TileSyncRequestPacket packet, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->
        {
            if (context.getSender().getEntityWorld().getDimension().getType().getId() == packet.dimensionID
                    && context.getSender().getEntityWorld().isBlockLoaded(packet.pos) &&
                    context.getSender().getEntityWorld().getTileEntity(packet.pos) != null)
                NetworkHandler.sendTileToPlayer(context.getSender().getEntityWorld().getTileEntity(packet.pos),
                        context.getSender());
        });
        context.setPacketHandled(true);
    }
}

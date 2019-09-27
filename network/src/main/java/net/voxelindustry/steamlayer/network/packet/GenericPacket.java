package net.voxelindustry.steamlayer.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

@NoArgsConstructor
public class GenericPacket
{
    private Message message;
    private int     packetID;

    public GenericPacket(Message message)
    {
        this.message = message;
        this.packetID = PacketHandler.getInstance().getID(message.getClass());
    }

    public static GenericPacket decode(ByteBuf buffer)
    {
        GenericPacket packet = new GenericPacket();
        packet.packetID = buffer.readInt();

        try
        {
            packet.message = PacketHandler.getInstance().byID(packet.packetID).newInstance();
        } catch (InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
        }

        packet.message.read(buffer);

        return packet;
    }

    public static void encode(GenericPacket packet, PacketBuffer buffer)
    {
        buffer.writeInt(packet.packetID);
        packet.message.write(buffer);
    }

    public static void handle(GenericPacket packet, Supplier<Context> contextSupplier)
    {
        Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isClient())
            context.enqueueWork(() -> packet.message.handle(Minecraft.getInstance().player));
        else
            context.enqueueWork(() -> packet.message.handle(context.getSender()));
    }
}

package net.voxelindustry.steamlayer.network.packet;

import lombok.NoArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

@NoArgsConstructor
public class GenericPacket
{
    private Message message;
    private int     packetID;

    public GenericPacket(Message message)
    {
        this.message = message;
        packetID = PacketHandler.getInstance().getID(message.getClass());
    }

    public static GenericPacket decode(PacketByteBuf buffer)
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

    public static void encode(GenericPacket packet, PacketByteBuf buffer)
    {
        buffer.writeInt(packet.packetID);
        packet.message.write(buffer);
    }

    public static void handle(GenericPacket packet, PacketContext context)
    {
        if (context.getPacketEnvironment() == EnvType.CLIENT)
            clientHandle(packet, context);
        else
            context.getTaskQueue().execute(() -> packet.message.handle(context.getPlayer()));
    }

    private static void clientHandle(GenericPacket packet, PacketContext context)
    {
        context.getTaskQueue().execute(() -> packet.message.handle(MinecraftClient.getInstance().player));
    }
}

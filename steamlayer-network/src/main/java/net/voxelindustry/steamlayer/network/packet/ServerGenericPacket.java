package net.voxelindustry.steamlayer.network.packet;

import lombok.NoArgsConstructor;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;

@NoArgsConstructor
public class ServerGenericPacket
{
    private Message message;
    private int     packetID;

    public ServerGenericPacket(Message message)
    {
        this.message = message;
        packetID = PacketHandler.getInstance().getID(message.getClass());
    }

    public ServerGenericPacket decode(PacketByteBuf buffer)
    {
        packetID = buffer.readInt();

        try
        {
            message = PacketHandler.getInstance().byID(packetID).newInstance();
        } catch (InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
        }

        message.read(buffer);

        return this;
    }

    public void encode(PacketByteBuf buffer)
    {
        buffer.writeInt(packetID);
        message.write(buffer);
    }

    public void handleServer(PacketContext context)
    {
        context.getTaskQueue().execute(() -> message.handle(context.getPlayer()));
    }
}

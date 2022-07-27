package net.voxelindustry.steamlayer.network.packet;

import lombok.NoArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.thread.ThreadExecutor;

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

    public void handleServer(PlayerEntity player, ThreadExecutor<? extends Runnable> threadExecutor)
    {
        threadExecutor.execute(() -> message.handle(player));
    }
}

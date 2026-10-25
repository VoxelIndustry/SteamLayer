package net.voxelindustry.steamlayer.network.packet;

import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;

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

    public ServerGenericPacket decode(FriendlyByteBuf buffer)
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

    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeInt(packetID);
        message.write(buffer);
    }

    public void handleServer(Player player, BlockableEventLoop<? extends Runnable> threadExecutor)
    {
        threadExecutor.execute(() -> message.handle(player));
    }
}

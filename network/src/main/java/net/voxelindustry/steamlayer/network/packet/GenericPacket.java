package net.voxelindustry.steamlayer.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@NoArgsConstructor
public class GenericPacket implements IMessage
{
    private Message message;
    private int     packetID;

    public GenericPacket(Message message)
    {
        this.message = message;
        this.packetID = PacketHandler.getInstance().getID(message.getClass());
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.packetID = buf.readInt();

        try
        {
            this.message = PacketHandler.getInstance().byID(this.packetID).newInstance();
        } catch (InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
        }

        this.message.read(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(packetID);
        this.message.write(buf);
    }

    private void handle(EntityPlayer player)
    {
        this.message.handle(player);
    }

    public static class GenericServerPacketHandler implements IMessageHandler<GenericPacket, IMessage>
    {
        @Override
        public IMessage onMessage(GenericPacket message, MessageContext ctx)
        {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> message.handle(ctx.getServerHandler().player));
            return null;
        }
    }

    public static class GenericClientPacketHandler implements IMessageHandler<GenericPacket, IMessage>
    {
        @Override
        public IMessage onMessage(GenericPacket message, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(() -> message.handle(Minecraft.getMinecraft().player));
            return null;
        }
    }
}

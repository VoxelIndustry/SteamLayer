package net.voxelindustry.steamlayer.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.voxelindustry.steamlayer.network.action.ActionManager;

import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
@Getter
public class ClientActionHolderPacket implements IMessage
{
    public static AtomicInteger previousActionID;

    private NBTTagCompound actionPayload;
    private int            replyID;

    public ClientActionHolderPacket(int replyID, NBTTagCompound payload)
    {
        this.actionPayload = payload;
        this.replyID = replyID;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(replyID);
        ByteBufUtils.writeTag(buf, actionPayload);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.replyID = buf.readInt();
        this.actionPayload = ByteBufUtils.readTag(buf);
    }


    @NoArgsConstructor
    public static class ClientActionHolderPacketHandler implements IMessageHandler<ClientActionHolderPacket, IMessage>
    {
        @Override
        public IMessage onMessage(ClientActionHolderPacket message, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(() ->
                    ActionManager.getInstance().triggerCallback(message.getReplyID(), message.getActionPayload()));
            return null;
        }
    }
}

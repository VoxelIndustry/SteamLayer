package net.voxelindustry.steamlayer.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.voxelindustry.steamlayer.common.container.ISyncableContainer;

@AllArgsConstructor
@NoArgsConstructor
public class ContainerUpdatePacket implements IMessage
{
    private int            windowID;
    private int            syncableID;
    private NBTTagCompound data;

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowID);
        buf.writeInt(syncableID);
        ByteBufUtils.writeTag(buf, data);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowID = buf.readInt();
        syncableID = buf.readInt();
        data = ByteBufUtils.readTag(buf);
    }

    @NoArgsConstructor
    public static class ContainerUpdatePacketHandler implements IMessageHandler<ContainerUpdatePacket, IMessage>
    {
        @Override
        public IMessage onMessage(ContainerUpdatePacket message, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(() ->
            {
                EntityPlayerSP player = Minecraft.getMinecraft().player;
                if (player.openContainer instanceof ISyncableContainer && player.openContainer.windowId == message.windowID)
                    ((ISyncableContainer) player.openContainer).updateProperty(message.syncableID, message.data);
            });
            return null;
        }
    }
}

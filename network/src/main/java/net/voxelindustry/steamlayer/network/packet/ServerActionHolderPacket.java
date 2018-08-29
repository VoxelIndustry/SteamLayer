package net.voxelindustry.steamlayer.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.voxelindustry.steamlayer.network.action.ActionSender;
import net.voxelindustry.steamlayer.network.action.IActionReceiver;

import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
@Getter
public class ServerActionHolderPacket implements IMessage
{
    public static AtomicInteger previousActionID = new AtomicInteger();

    private String actionName;

    @Setter
    private NBTTagCompound actionPayload;
    private BlockPos       pos;
    private int            dimension;
    private int            actionID;

    @Setter
    boolean expectAnswer;

    public ServerActionHolderPacket(TileEntity to, String name)
    {
        this.actionName = name;
        this.dimension = to.getWorld().provider.getDimension();
        this.pos = to.getPos();

        this.actionID = previousActionID.getAndUpdate(previous -> previous > 32765 ? 0 : previous + 1);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        buf.writeShort(actionID);
        buf.writeInt(dimension);
        buf.writeLong(pos.toLong());
        buf.writeBoolean(expectAnswer);

        ByteBufUtils.writeUTF8String(buf, actionName);
        ByteBufUtils.writeTag(buf, actionPayload);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        actionID = buf.readShort();
        dimension = buf.readInt();
        pos = BlockPos.fromLong(buf.readLong());
        expectAnswer = buf.readBoolean();

        actionName = ByteBufUtils.readUTF8String(buf);
        actionPayload = ByteBufUtils.readTag(buf);
    }

    @NoArgsConstructor
    public static class ServerActionHolderPacketHandler implements IMessageHandler<ServerActionHolderPacket, IMessage>
    {
        @Override
        public IMessage onMessage(ServerActionHolderPacket message, MessageContext ctx)
        {
            EntityPlayerMP serverPlayer = ctx.getServerHandler().player;

            serverPlayer.getServerWorld().addScheduledTask(() ->
            {
                World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(message.getDimension());

                if (world.isBlockLoaded(message.getPos()))
                {
                    TileEntity receiver = world.getTileEntity(message.getPos());

                    if (receiver instanceof IActionReceiver)
                    {
                        ActionSender actionSender = new ActionSender(serverPlayer, receiver, message.getActionID());
                        ((IActionReceiver) receiver).handle(actionSender, message.getActionName(),
                                message.getActionPayload());
                        if (message.isExpectAnswer() && !actionSender.isAnswered())
                            actionSender.answer().send();
                    }
                }
            });
            return null;
        }
    }
}

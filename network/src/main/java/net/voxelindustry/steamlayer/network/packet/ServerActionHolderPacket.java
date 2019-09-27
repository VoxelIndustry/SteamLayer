package net.voxelindustry.steamlayer.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.voxelindustry.steamlayer.network.ByteBufHelper;
import net.voxelindustry.steamlayer.network.action.ActionSender;
import net.voxelindustry.steamlayer.network.action.IActionReceiver;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@NoArgsConstructor
@Getter
public class ServerActionHolderPacket
{
    public static AtomicInteger previousActionID = new AtomicInteger();

    private String actionName;

    @Setter
    private CompoundNBT actionPayload;
    private BlockPos    pos;
    private int         dimension;
    private int         actionID;

    @Setter
    boolean expectAnswer;

    public ServerActionHolderPacket(TileEntity to, String name)
    {
        actionName = name;
        dimension = to.getWorld().getDimension().getType().getId();
        pos = to.getPos();

        actionID = previousActionID.getAndUpdate(previous -> previous > 32765 ? 0 : previous + 1);
    }

    public static ServerActionHolderPacket decode(ByteBuf buffer)
    {
        ServerActionHolderPacket packet = new ServerActionHolderPacket();

        packet.actionID = buffer.readShort();
        packet.dimension = buffer.readInt();
        packet.pos = ByteBufHelper.readPos(buffer);
        packet.expectAnswer = buffer.readBoolean();

        packet.actionName = ByteBufHelper.readString(buffer);
        packet.actionPayload = ByteBufHelper.readTag(buffer);

        return packet;
    }

    public static void encode(ServerActionHolderPacket packet, ByteBuf buffer)
    {
        buffer.writeShort(packet.actionID);
        buffer.writeInt(packet.dimension);
        ByteBufHelper.writePos(buffer, packet.pos);
        buffer.writeBoolean(packet.expectAnswer);

        ByteBufHelper.writeString(buffer, packet.actionName);
        ByteBufHelper.writeTag(buffer, packet.actionPayload);
    }

    public static void handle(ServerActionHolderPacket packet, Supplier<Context> contextSupplier)
    {
        contextSupplier.get().enqueueWork(() ->
        {
            ServerWorld world = ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.getById(packet.dimension));

            if (world.isBlockLoaded(packet.getPos()))
            {
                TileEntity receiver = world.getTileEntity(packet.getPos());

                if (receiver instanceof IActionReceiver)
                {
                    ActionSender actionSender = new ActionSender(contextSupplier.get().getSender(), receiver, packet.getActionID());
                    ((IActionReceiver) receiver).handle(actionSender, packet.getActionName(),
                            packet.getActionPayload());
                    if (packet.isExpectAnswer() && !actionSender.isAnswered())
                        actionSender.answer().send();
                }
            }
        });
    }
}

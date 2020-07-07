package net.voxelindustry.steamlayer.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.network.NetworkEvent;
import net.voxelindustry.steamlayer.network.ByteBufHelper;
import net.voxelindustry.steamlayer.network.action.ActionManager;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@NoArgsConstructor
@Getter
public class ClientActionHolderPacket
{
    public static AtomicInteger previousActionID;

    private CompoundNBT actionPayload;
    private int         replyID;

    public ClientActionHolderPacket(int replyID, CompoundNBT payload)
    {
        actionPayload = payload;
        this.replyID = replyID;
    }

    public static ClientActionHolderPacket decode(ByteBuf buffer)
    {
        ClientActionHolderPacket packet = new ClientActionHolderPacket();
        packet.replyID = buffer.readInt();
        packet.actionPayload = ByteBufHelper.readTag(buffer);

        return packet;
    }

    public static void encode(ClientActionHolderPacket packet, ByteBuf buffer)
    {
        buffer.writeInt(packet.replyID);
        ByteBufHelper.writeTag(buffer, packet.actionPayload);
    }

    public static void handle(ClientActionHolderPacket packet, Supplier<NetworkEvent.Context> contextSupplier)
    {
        contextSupplier.get().enqueueWork(() -> ActionManager.getInstance().triggerCallback(packet.replyID, packet.actionPayload));
        contextSupplier.get().setPacketHandled(true);
    }
}

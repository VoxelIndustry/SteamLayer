package net.voxelindustry.steamlayer.network.packet;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.thread.BlockableEventLoop;
import net.voxelindustry.steamlayer.network.action.ActionManager;

import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
@Getter
public class ClientActionHolderPacket
{
    public static AtomicInteger previousActionID;

    private CompoundTag actionPayload;
    private int         replyID;

    public ClientActionHolderPacket(int replyID, CompoundTag payload)
    {
        actionPayload = payload;
        this.replyID = replyID;
    }

    public static ClientActionHolderPacket decode(FriendlyByteBuf buffer)
    {
        ClientActionHolderPacket packet = new ClientActionHolderPacket();
        packet.replyID = buffer.readInt();
        packet.actionPayload = buffer.readNbt();

        return packet;
    }

    public static void encode(ClientActionHolderPacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeInt(packet.replyID);
        buffer.writeNbt(packet.actionPayload);
    }

    public static void handleClient(ClientActionHolderPacket packet, BlockableEventLoop<? extends Runnable> threadExecutor)
    {
        threadExecutor.execute(() -> ActionManager.getInstance().triggerCallback(packet.replyID, packet.actionPayload));
    }
}

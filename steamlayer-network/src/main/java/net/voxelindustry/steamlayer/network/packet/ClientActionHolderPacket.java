package net.voxelindustry.steamlayer.network.packet;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.voxelindustry.steamlayer.network.action.ActionManager;

import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
@Getter
public class ClientActionHolderPacket
{
    public static AtomicInteger previousActionID;

    private NbtCompound actionPayload;
    private int         replyID;

    public ClientActionHolderPacket(int replyID, NbtCompound payload)
    {
        actionPayload = payload;
        this.replyID = replyID;
    }

    public static ClientActionHolderPacket decode(PacketByteBuf buffer)
    {
        ClientActionHolderPacket packet = new ClientActionHolderPacket();
        packet.replyID = buffer.readInt();
        packet.actionPayload = buffer.readNbt();

        return packet;
    }

    public static void encode(ClientActionHolderPacket packet, PacketByteBuf buffer)
    {
        buffer.writeInt(packet.replyID);
        buffer.writeNbt(packet.actionPayload);
    }

    public static void handleClient(ClientActionHolderPacket packet, PacketContext context)
    {
        context.getTaskQueue().execute(() -> ActionManager.getInstance().triggerCallback(packet.replyID, packet.actionPayload));
    }
}

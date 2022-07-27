package net.voxelindustry.steamlayer.network.packet;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.thread.ThreadExecutor;
import net.voxelindustry.steamlayer.network.action.ActionSender;
import net.voxelindustry.steamlayer.network.action.IActionReceiver;

import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
@Getter
public class ServerActionHolderPacket
{
    public static AtomicInteger previousActionID = new AtomicInteger();

    private String actionName;

    @Setter
    private NbtCompound actionPayload;
    private BlockPos    pos;
    private String      dimensionKey;
    private int         actionID;

    @Setter
    boolean expectAnswer;

    public ServerActionHolderPacket(BlockEntity to, String name)
    {
        actionName = name;
        dimensionKey = to.getWorld().getRegistryKey().getValue().toString();
        pos = to.getPos();

        actionID = previousActionID.getAndUpdate(previous -> previous > 32765 ? 0 : previous + 1);
    }

    public static ServerActionHolderPacket decode(PacketByteBuf buffer)
    {
        ServerActionHolderPacket packet = new ServerActionHolderPacket();

        packet.actionID = buffer.readShort();
        packet.dimensionKey = buffer.readString();
        packet.pos = buffer.readBlockPos();
        packet.expectAnswer = buffer.readBoolean();

        packet.actionName = buffer.readString();
        packet.actionPayload = buffer.readNbt();

        return packet;
    }

    public static void encode(ServerActionHolderPacket packet, PacketByteBuf buffer)
    {
        buffer.writeShort(packet.actionID);
        buffer.writeString(packet.dimensionKey);
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.expectAnswer);

        buffer.writeString(packet.actionName);
        buffer.writeNbt(packet.actionPayload);
    }

    public static void handleServer(ServerActionHolderPacket packet, PlayerEntity player, ThreadExecutor<? extends Runnable> threadExecutor)
    {
        threadExecutor.execute(() ->
        {
            var world = player.getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, new Identifier(packet.dimensionKey)));

            var chunkPos = new ChunkPos(packet.getPos());

            if (!world.isChunkLoaded(chunkPos.x, chunkPos.z))
                return;

            var receiver = world.getBlockEntity(packet.getPos());

            if (receiver instanceof IActionReceiver actionReceiver)
            {
                var actionSender = new ActionSender(player, receiver, packet.getActionID());
                actionReceiver.handle(actionSender, packet.getActionName(), packet.getActionPayload());

                if (packet.isExpectAnswer() && !actionSender.isAnswered())
                    actionSender.answer().send();
            }
        });
    }
}

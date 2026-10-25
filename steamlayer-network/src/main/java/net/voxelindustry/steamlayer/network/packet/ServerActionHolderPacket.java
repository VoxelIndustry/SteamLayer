package net.voxelindustry.steamlayer.network.packet;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
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
    private CompoundTag actionPayload;
    private BlockPos    pos;
    private String      dimensionKey;
    private int         actionID;

    @Setter
    boolean expectAnswer;

    public ServerActionHolderPacket(BlockEntity to, String name)
    {
        actionName = name;
        dimensionKey = to.getLevel().dimension().location().toString();
        pos = to.getBlockPos();

        actionID = previousActionID.getAndUpdate(previous -> previous > 32765 ? 0 : previous + 1);
    }

    public static ServerActionHolderPacket decode(FriendlyByteBuf buffer)
    {
        ServerActionHolderPacket packet = new ServerActionHolderPacket();

        packet.actionID = buffer.readShort();
        packet.dimensionKey = buffer.readUtf();
        packet.pos = buffer.readBlockPos();
        packet.expectAnswer = buffer.readBoolean();

        packet.actionName = buffer.readUtf();
        packet.actionPayload = buffer.readNbt();

        return packet;
    }

    public static void encode(ServerActionHolderPacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeShort(packet.actionID);
        buffer.writeUtf(packet.dimensionKey);
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.expectAnswer);

        buffer.writeUtf(packet.actionName);
        buffer.writeNbt(packet.actionPayload);
    }

    public static void handleServer(ServerActionHolderPacket packet, Player player, BlockableEventLoop<? extends Runnable> threadExecutor)
    {
        threadExecutor.execute(() ->
        {
            var world = player.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(packet.dimensionKey)));

            var chunkPos = new ChunkPos(packet.getPos());

            if (!world.hasChunk(chunkPos.x, chunkPos.z))
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

package net.voxelindustry.steamlayer.container.sync;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.voxelindustry.steamlayer.common.container.ISyncedContainer;
import net.voxelindustry.steamlayer.network.packet.Message;

@NoArgsConstructor
public class ContainerSyncPacket extends Message
{
    private int         windowID;
    private int         syncID;
    private SyncedValue property;

    private ByteBuf bufferCopy;

    public ContainerSyncPacket(int windowID, int syncID, SyncedValue property)
    {
        this.windowID = windowID;
        this.syncID = syncID;
        this.property = property;
    }

    @Override
    public void read(FriendlyByteBuf buf)
    {
        windowID = buf.readInt();
        syncID = buf.readInt();

        bufferCopy = buf.copy();
    }

    @Override
    public void write(FriendlyByteBuf buf)
    {
        buf.writeInt(windowID);
        buf.writeInt(syncID);

        property.write(buf);
    }

    @Override
    public void handle(Player player)
    {
        if (player.containerMenu instanceof ISyncedContainer && player.containerMenu.containerId == windowID)
            ((ISyncedContainer) player.containerMenu).updateProperty(syncID, bufferCopy);
    }
}

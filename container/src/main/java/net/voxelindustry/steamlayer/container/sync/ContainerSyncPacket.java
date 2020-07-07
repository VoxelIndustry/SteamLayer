package net.voxelindustry.steamlayer.container.sync;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;
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
    public void read(ByteBuf buf)
    {
        windowID = buf.readInt();
        syncID = buf.readInt();

        bufferCopy = buf.copy();
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeInt(windowID);
        buf.writeInt(syncID);

        property.write(buf);
    }

    @Override
    public void handle(PlayerEntity player)
    {
        if (player.openContainer instanceof ISyncedContainer && player.openContainer.windowId == windowID)
            ((ISyncedContainer) player.openContainer).updateProperty(syncID, bufferCopy);
    }
}

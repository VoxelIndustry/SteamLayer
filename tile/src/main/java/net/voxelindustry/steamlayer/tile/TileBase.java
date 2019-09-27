package net.voxelindustry.steamlayer.tile;

import lombok.Getter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.voxelindustry.steamlayer.network.NetworkHandler;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;
import net.voxelindustry.steamlayer.network.packet.TileSyncRequestPacket;

public class TileBase extends TileEntity implements ITileInfoProvider
{
    @Getter
    private boolean isSyncLocked;
    @Getter
    private boolean isSyncQueued;

    public TileBase(TileEntityType<?> type)
    {
        super(type);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        CompoundNBT nbtTag = new CompoundNBT();
        write(nbtTag);
        return new SUpdateTileEntityPacket(pos, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
    {
        read(packet.getNbtCompound());
    }

    public void askServerSync()
    {
        SteamLayerPacketHandler.getHandler().sendToServer(new TileSyncRequestPacket(
                world.getDimension().getType().getId(),
                getPos()));
    }

    public void forceSync()
    {
        if (world != null)
        {
            NetworkHandler.sendTileToRange(this);
            isSyncQueued = false;
        }
    }

    public void sync()
    {
        if (world != null)
        {
            if (!isSyncLocked())
                NetworkHandler.sendTileToRange(this);
            else
                isSyncQueued = true;
        }
    }

    public void syncLock()
    {
        isSyncLocked = true;
    }

    public void releaseSyncLock(boolean flushSync)
    {
        isSyncLocked = false;

        if (flushSync && isSyncQueued)
            sync();
        isSyncQueued = false;
    }

    public boolean isServer()
    {
        if (world != null)
            return !world.isRemote;
        return EffectiveSide.get().isServer();
    }

    public boolean isClient()
    {
        if (world != null)
            return world.isRemote;
        return EffectiveSide.get().isClient();
    }

    @Override
    public void addInfo(ITileInfoList list)
    {

    }
}

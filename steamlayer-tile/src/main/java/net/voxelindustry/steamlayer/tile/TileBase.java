package net.voxelindustry.steamlayer.tile;

import lombok.Getter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.voxelindustry.steamlayer.network.NetworkHandler;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;
import net.voxelindustry.steamlayer.network.packet.TileSyncRequestPacket;

import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.TILE_SYNC_REQUEST;

public class TileBase extends BlockEntity implements ITileInfoProvider, ISyncTile
{
    @Getter
    private boolean isSyncLocked;
    @Getter
    private boolean isSyncQueued;

    public TileBase(BlockEntityType<?> type)
    {
        super(type);
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket()
    {
        CompoundTag nbtTag = new CompoundTag();
        toTag(nbtTag);
        return new BlockEntityUpdateS2CPacket(pos, 1, nbtTag);
    }

    @Override
    public void askServerSync()
    {
        SteamLayerPacketHandler.createServerBoundPacket(TILE_SYNC_REQUEST,
                buffer -> TileSyncRequestPacket.encode(
                        new TileSyncRequestPacket(
                                world.getRegistryKey().getValue().toString(),
                                getPos()), buffer)
        );
    }

    @Override
    public void forceSync()
    {
        if (world != null)
        {
            NetworkHandler.sendTileToRange(this);
            isSyncQueued = false;
        }
    }

    @Override
    public void fromClientTag(CompoundTag tag)
    {
        fromTag(null, tag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag)
    {
        return toTag(tag);
    }

    @Override
    public void sync()
    {
        World world = getWorld();
        if (world != null && !world.isClient())
        {
            if (!isSyncLocked())
                ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
            else
                isSyncQueued = true;
        }
    }

    @Override
    public void syncLock()
    {
        isSyncLocked = true;
    }

    @Override
    public void releaseSyncLock(boolean flushSync)
    {
        isSyncLocked = false;

        if (flushSync && isSyncQueued)
            sync();
        isSyncQueued = false;
    }

    public boolean isServer()
    {
        return !getWorld().isClient();
    }

    public boolean isClient()
    {
        return getWorld().isClient();
    }

    @Override
    public void addInfo(ITileInfoList list)
    {

    }
}
package net.voxelindustry.steamlayer.tile;

import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;
import net.voxelindustry.steamlayer.network.packet.TileSyncRequestPacket;
import net.voxelindustry.steamlayer.network.tilesync.PartialSyncedTile;
import net.voxelindustry.steamlayer.network.tilesync.PartialTileSyncRequestPacket;

import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.TILE_SYNC_REQUEST;

public class TileBase extends BlockEntity implements ITileInfoProvider, ISyncTile, ILoadable
{
    @Getter
    private boolean isSyncLocked;
    @Getter
    private boolean isSyncQueued;

    public TileBase(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
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
    public NbtCompound toInitialChunkDataNbt()
    {
        return createNbt();
    }

    @Override
    public void forceSync()
    {
        if (world != null)
        {
            sync();
            isSyncQueued = false;
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket()
    {
        return BlockEntityUpdateS2CPacket.create(this);
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
        if (getWorld() != null)
            return !getWorld().isClient();

        return Thread.currentThread().getName().contains("Server");
    }

    public boolean isClient()
    {
        if (getWorld() != null)
            return getWorld().isClient();

        return !Thread.currentThread().getName().contains("Server");
    }

    @Override
    public void addInfo(ITileInfoList list)
    {

    }

    @Override
    public void clientLoad()
    {
        if (this instanceof PartialSyncedTile && ((PartialSyncedTile) this).syncOnLoad())
            new PartialTileSyncRequestPacket(getPos(), getWorld().getRegistryKey().getValue(), ((PartialSyncedTile) this).getAllSyncElements())
                    .sendToServer();
    }
}

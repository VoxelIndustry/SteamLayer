package net.voxelindustry.steamlayer.tile;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.voxelindustry.steamlayer.network.SteamLayerClientPacketHandler;
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
        SteamLayerClientPacketHandler.sendToServer(SteamLayerPacketHandler.createServerBoundPacket(TILE_SYNC_REQUEST,
                buffer -> TileSyncRequestPacket.encode(
                        new TileSyncRequestPacket(
                                level.dimension().identifier().toString(),
                                getBlockPos()), buffer)
        ));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void forceSync()
    {
        if (level != null)
        {
            sync();
            isSyncQueued = false;
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void sync()
    {
        Level world = getLevel();
        if (world != null && !world.isClientSide())
        {
            if (!isSyncLocked())
                ((ServerLevel) world).getChunkSource().blockChanged(getBlockPos());
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
        if (getLevel() != null)
            return !getLevel().isClientSide();

        return Thread.currentThread().getName().contains("Server");
    }

    public boolean isClient()
    {
        if (getLevel() != null)
            return getLevel().isClientSide();

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
            new PartialTileSyncRequestPacket(getBlockPos(), getLevel().dimension().identifier(), ((PartialSyncedTile) this).getAllSyncElements())
                    .sendToServer();
    }
}

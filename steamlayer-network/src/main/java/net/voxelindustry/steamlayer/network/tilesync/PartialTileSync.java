package net.voxelindustry.steamlayer.network.tilesync;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.Optional;

@Log4j2
@UtilityClass
public class PartialTileSync
{
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity & PartialSyncedTile> void syncPart(T tile, Identifier identifier)
    {
        syncPart(tile, tile, identifier);
    }

    public void syncPart(PartialSyncedTile syncedTile, BlockEntity worldTile, Identifier identifier)
    {
        if (worldTile.getLevel().isClientSide())
            return;

        Optional<TileSyncElement<Object>> syncElementOpt = (Optional<TileSyncElement<Object>>) (Object) syncedTile.getSyncElement(identifier);

        syncElementOpt.flatMap(syncElement ->
                syncElement.getCodec()
                        .encodeStart(worldTile.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE), syncElement)
                        .resultOrPartial(error ->
                        {
                            throw new TileSyncException(worldTile.getClass(), worldTile, error);
                        })).ifPresent(tag ->
        {
            new PartialTileSyncPacket(worldTile.getBlockPos(), worldTile.getLevel().dimension().identifier(), (CompoundTag) tag, identifier)
                    .sendToTracking(worldTile);
        });
    }

    public void receiveSync(PartialSyncedTile tile, BlockEntity worldTile, Identifier elementIdentifier, CompoundTag tag)
    {
        tile.getSyncElementCodec(elementIdentifier)
                .decode(worldTile.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE), tag)
                .resultOrPartial(error ->
                {
                    throw new TileSyncException(worldTile.getClass(), worldTile, error);
                })
                .ifPresent(elementWithTag ->
                {
                    tile.receiveSyncElement((TileSyncElement<?>) elementWithTag.getFirst());
                });
    }
}

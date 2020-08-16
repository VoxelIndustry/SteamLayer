package net.voxelindustry.steamlayer.network.tilesync;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.datafixer.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

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
        if (worldTile.getWorld().isClient())
            return;

        Optional<TileSyncElement<Object>> syncElementOpt = (Optional<TileSyncElement<Object>>) (Object) syncedTile.getSyncElement(identifier);

        syncElementOpt.flatMap(syncElement ->
        {
            return syncElement.getCodec()
                    .encodeStart(NbtOps.INSTANCE, syncElement)
                    .resultOrPartial(log::error);
        }).ifPresent(tag ->
        {
            new PartialTileSyncPacket(worldTile.getPos(), worldTile.getWorld().getRegistryKey().getValue(), (CompoundTag) tag, identifier)
                    .sendToTracking(worldTile);
        });
    }

    public void receiveSync(PartialSyncedTile tile, Identifier elementIdentifier, CompoundTag tag)
    {
        tile.getSyncElementCodec(elementIdentifier)
                .decode(NbtOps.INSTANCE, tag)
                .resultOrPartial(log::error)
                .ifPresent(elementWithTag ->
                {
                    tile.receiveSyncElement((TileSyncElement<?>) elementWithTag.getFirst());
                });
    }
}

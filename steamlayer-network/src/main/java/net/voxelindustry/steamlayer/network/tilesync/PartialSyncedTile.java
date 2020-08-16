package net.voxelindustry.steamlayer.network.tilesync;

import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Optional;

public interface PartialSyncedTile
{
    Optional<TileSyncElement<?>> getSyncElement(Identifier identifier);

    void receiveSyncElement(TileSyncElement<?> element);

    Codec<?> getSyncElementCodec(Identifier identifier);

    Collection<Identifier> getAllSyncElements();

    default boolean syncOnLoad()
    {
        return true;
    }
}

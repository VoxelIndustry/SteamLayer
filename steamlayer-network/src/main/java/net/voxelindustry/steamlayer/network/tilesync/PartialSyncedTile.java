package net.voxelindustry.steamlayer.network.tilesync;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public interface PartialSyncedTile
{
    Optional<TileSyncElement<?>> getSyncElement(ResourceLocation identifier);

    void receiveSyncElement(TileSyncElement<?> element);

    Codec<?> getSyncElementCodec(ResourceLocation identifier);

    Collection<ResourceLocation> getAllSyncElements();

    default boolean syncOnLoad()
    {
        return true;
    }
}

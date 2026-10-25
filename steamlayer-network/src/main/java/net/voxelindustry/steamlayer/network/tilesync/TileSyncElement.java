package net.voxelindustry.steamlayer.network.tilesync;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public interface TileSyncElement<SELF>
{
    ResourceLocation getIdentifier();

    Codec<SELF> getCodec();
}

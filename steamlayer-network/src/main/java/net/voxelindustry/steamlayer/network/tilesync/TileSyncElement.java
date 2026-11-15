package net.voxelindustry.steamlayer.network.tilesync;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;

public interface TileSyncElement<SELF>
{
    Identifier getIdentifier();

    Codec<SELF> getCodec();
}

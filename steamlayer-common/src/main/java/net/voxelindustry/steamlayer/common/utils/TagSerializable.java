package net.voxelindustry.steamlayer.common.utils;

import net.minecraft.nbt.NbtElement;

public interface TagSerializable<T extends NbtElement>
{
    T toTag();

    void fromTag(T tag);
}
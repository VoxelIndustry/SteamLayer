package net.voxelindustry.steamlayer.common.utils;

import net.minecraft.nbt.Tag;

public interface TagSerializable<T extends Tag>
{
    T toTag();

    void fromTag(T tag);
}
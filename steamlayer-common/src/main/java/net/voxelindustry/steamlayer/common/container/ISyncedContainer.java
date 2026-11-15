package net.voxelindustry.steamlayer.common.container;

import io.netty.buffer.ByteBuf;

public interface ISyncedContainer
{
    void updateProperty(int id, ByteBuf buffer);
}

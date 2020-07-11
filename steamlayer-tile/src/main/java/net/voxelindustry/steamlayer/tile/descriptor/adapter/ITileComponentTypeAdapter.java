package net.voxelindustry.steamlayer.tile.descriptor.adapter;

import com.google.gson.stream.JsonReader;
import net.voxelindustry.steamlayer.tile.descriptor.ITileComponent;

import java.io.IOException;

public interface ITileComponentTypeAdapter<T extends ITileComponent>
{
    Class<T> getComponentClass();

    T read(JsonReader in) throws IOException;
}

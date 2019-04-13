package net.voxelindustry.steamlayer.tile.descriptor.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.voxelindustry.steamlayer.tile.descriptor.ModularTiles;
import net.voxelindustry.steamlayer.tile.descriptor.TileDescriptor;

import java.io.IOException;
import java.util.HashMap;

public class TileDescriptorTypeAdapter extends TypeAdapter<TileDescriptor>
{
    private HashMap<String, ITileComponentTypeAdapter> subTypeAdapters;

    private final ModularTiles modularSingleton;

    public TileDescriptorTypeAdapter(ModularTiles instance)
    {
        this.subTypeAdapters = new HashMap<>();

        this.modularSingleton = instance;
    }

    public void addSubTypeAdapter(String key, ITileComponentTypeAdapter adapter)
    {
        this.subTypeAdapters.put(key, adapter);
    }

    @Override
    public void write(JsonWriter out, TileDescriptor value)
    {

    }

    @Override
    public TileDescriptor read(JsonReader in) throws IOException
    {
        TileDescriptor descriptor = new TileDescriptor();

        in.beginObject();
        while (in.hasNext())
        {
            if ("components".equals(in.nextName()))
            {
                descriptor.setComponents(new HashMap<>());
                in.beginObject();
                while (in.hasNext())
                {
                    if (modularSingleton.isPreloading())
                    {
                        descriptor.getComponents().put(
                                this.subTypeAdapters.get(in.nextName()).getComponentClass(), null);
                        in.skipValue();
                    }
                    else
                        descriptor.component(this.subTypeAdapters.get(in.nextName()).read(in));
                }
                in.endObject();
            }
        }
        in.endObject();
        return descriptor;
    }
}

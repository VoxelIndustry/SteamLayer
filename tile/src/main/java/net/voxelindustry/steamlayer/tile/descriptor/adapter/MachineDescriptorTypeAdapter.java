package net.voxelindustry.steamlayer.tile.descriptor.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.ros.common.machine.EMachineTier;
import net.ros.common.machine.EMachineType;
import net.ros.common.machine.MachineDescriptor;
import net.ros.common.machine.Machines;
import net.voxelindustry.steamlayer.tile.descriptor.TileDescriptor;

import java.io.IOException;
import java.util.HashMap;

public class MachineDescriptorTypeAdapter extends TypeAdapter<TileDescriptor>
{
    private HashMap<String, MachineDescriptorTypeAdapter> subTypeAdapters;

    public MachineDescriptorTypeAdapter()
    {
        subTypeAdapters = new HashMap<>();

        subTypeAdapters.put("steam", new SteamComponentTypeAdapter());
        subTypeAdapters.put("multiblock", new MultiblockComponentTypeAdapter());
        subTypeAdapters.put("blueprint", new BlueprintComponentTypeAdapter());
        subTypeAdapters.put("crafter", new CraftingComponentTypeAdapter());
        subTypeAdapters.put("automation", new AutomationComponentTypeAdapter());
        subTypeAdapters.put("io", new IOComponentTypeAdapter());
        subTypeAdapters.put("fluid", new FluidComponentTypeAdapter());
    }

    @Override
    public void write(JsonWriter out, MachineDescriptor value)
    {

    }

    @Override
    public TileDescriptor read(JsonReader in) throws IOException
    {
        TileDescriptor descriptor = new TileDescriptor();

        in.beginObject();
        while (in.hasNext())
        {
            switch (in.nextName())
            {
                case "components":
                    descriptor.setComponents(new HashMap<>());
                    in.beginObject();
                    while (in.hasNext())
                    {
                        if (Machines.isPreloading())
                        {
                            descriptor.getComponents().put(
                                    this.subTypeAdapters.get(in.nextName()).getComponentClass(), null);
                            in.skipValue();
                        }
                        else
                            descriptor.component(this.subTypeAdapters.get(in.nextName()).read(in));
                    }
                    in.endObject();
                    break;
                default:
                    break;
            }
        }
        in.endObject();
        return descriptor;
    }
}

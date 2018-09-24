package net.voxelindustry.steamlayer.tile.descriptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TileComponentLoader
{
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(TileDescriptor.class, new TileDescriptorTypeAdapter()).create();

    public static TileDescriptor loadTile(String modid, String name)
    {
        TileDescriptor descriptor = null;
        final InputStream stream = TileComponentLoader.class
                .getResourceAsStream("/assets/" + modid + "/machines/" + name + ".hjson");
        try
        {
            descriptor = GSON.fromJson(JsonValue.readHjson(IOUtils.toString(stream, StandardCharsets.UTF_8)).toString(),
                    TileDescriptor.class);
            stream.close();
            descriptor.setName(name);
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (ParseException e)
        {
            SteamLayerConstants.LOGGER.error("Exception thrown while parsing " + name, e);
        }
        return descriptor;
    }
}

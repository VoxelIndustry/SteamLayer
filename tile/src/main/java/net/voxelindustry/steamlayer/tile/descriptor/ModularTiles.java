package net.voxelindustry.steamlayer.tile.descriptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.ResourceLocation;
import net.voxelindustry.steamlayer.tile.descriptor.adapter.ITileComponentTypeAdapter;
import net.voxelindustry.steamlayer.tile.descriptor.adapter.TileDescriptorTypeAdapter;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hjson.JsonValue;
import org.hjson.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ModularTiles
{
    private static final Map<String, ModularTiles> instances = new IdentityHashMap<>();

    public static ModularTiles instance(String modid)
    {
        if (!instances.containsKey(modid))
            instances.put(modid, new ModularTiles(modid));
        return instances.get(modid);
    }

    private final String modid;
    private final Gson   gson;
    private final Logger logger;

    private final TileDescriptorTypeAdapter tileAdapter;

    private boolean preload;

    private Map<String, TileDescriptor>                               tiles    = new HashMap<>();
    private Map<Class<? extends ITileComponent>, Set<TileDescriptor>> subLists = new IdentityHashMap<>();

    private List<ResourceLocation> toLoad = new ArrayList<>();

    private ModularTiles(String modid)
    {
        this.modid = modid;

        this.tileAdapter = new TileDescriptorTypeAdapter(this);

        this.gson = new GsonBuilder()
                .registerTypeAdapter(TileDescriptor.class, new TileDescriptorTypeAdapter(this)).create();

        this.logger = LogManager.getLogger("ModularTile-" + modid);
    }

    public void preloadTiles()
    {
        preload = true;
        doLoad();
    }

    public void loadTiles()
    {
        preload = false;
        doLoad();
    }

    private void doLoad()
    {
        if (!isPreloading())
        {
            tiles.clear();
            subLists.clear();
        }

        this.toLoad.forEach(rsl -> tiles.put(rsl.getPath(), loadTile(rsl)));
    }

    public void addTile(ResourceLocation rsl)
    {
        this.toLoad.add(rsl);
    }

    public void registerAdapter(String key, ITileComponentTypeAdapter adapter)
    {
        this.tileAdapter.addSubTypeAdapter(key, adapter);
    }

    public Set<TileDescriptor> getAllByComponent(Class<? extends ITileComponent> componentType)
    {
        if (!subLists.containsKey(componentType))
            subLists.put(componentType,
                    tiles.values().stream().filter(descriptor -> descriptor.has(componentType))
                            .collect(Collectors.toSet()));
        return subLists.get(componentType);
    }

    public Collection<TileDescriptor> getAll()
    {
        return tiles.values();
    }

    public boolean contains(Class<? extends ITileComponent> componentType, String name)
    {
        return getAllByComponent(componentType).stream().anyMatch(descriptor -> descriptor.getName().equals(name));
    }

    public <T extends ITileComponent> T getComponent(Class<T> componentType, String name)
    {
        Optional<TileDescriptor> desc = getAllByComponent(componentType).stream()
                .filter(descriptor -> descriptor.getName().equals(name)).findAny();

        return desc.map(tileDescriptor -> tileDescriptor.get(componentType)).orElse(null);
    }

    public TileDescriptor get(String name)
    {
        return tiles.get(name);
    }

    public boolean isPreloading()
    {
        return preload;
    }

    private TileDescriptor loadTile(ResourceLocation rsl)
    {
        TileDescriptor descriptor = null;
        InputStream stream = ModularTiles.class
                .getResourceAsStream("/assets/" + rsl.getNamespace() + "/tiles/" + rsl.getPath() + ".hjson");
        try
        {
            descriptor = gson.fromJson(JsonValue.readHjson(IOUtils.toString(stream, StandardCharsets.UTF_8)).toString(),
                    TileDescriptor.class);
            stream.close();
            descriptor.setName(rsl.getPath());
            tiles.put(rsl.getPath(), descriptor);
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (ParseException e)
        {
            logger.error("Exception thrown while parsing " + rsl.getPath(), e);
        }
        return descriptor;
    }
}

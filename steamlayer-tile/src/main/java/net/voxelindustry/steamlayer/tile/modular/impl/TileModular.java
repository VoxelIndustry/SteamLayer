package net.voxelindustry.steamlayer.tile.modular.impl;

import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.voxelindustry.steamlayer.tile.TileBase;
import net.voxelindustry.steamlayer.tile.descriptor.ModularTiles;
import net.voxelindustry.steamlayer.tile.descriptor.TileDescriptor;
import net.voxelindustry.steamlayer.tile.modular.IModularTile;
import net.voxelindustry.steamlayer.tile.modular.ISerializableModule;
import net.voxelindustry.steamlayer.tile.modular.TileModule;

import java.util.Collection;
import java.util.HashMap;

public class TileModular extends TileBase implements IModularTile
{
    private HashMap<Class<? extends TileModule>, TileModule> modules;

    @Getter
    private TileDescriptor descriptor;
    private String         modid;

    public TileModular(String modid, BlockEntityType<?> type, BlockPos pos, BlockState state, TileDescriptor descriptor)
    {
        super(type, pos, state);

        this.descriptor = descriptor;
        this.modid = modid;

        modules = new HashMap<>();
        if (descriptor != null)
            reloadModules();
    }

    public TileModular(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        this(null, type, pos, state, null);
    }

    @Override
    public void readNbt(NbtCompound tag)
    {
        super.readNbt(tag);

        modid = tag.getString("modid");
        TileDescriptor previous = descriptor;
        descriptor = ModularTiles.instance(modid).get(tag.getString("tileDescriptor"));

        if (previous == null && descriptor != null)
            reloadModules();

        modules.values().forEach(module ->
        {
            if (module instanceof ISerializableModule)
                ((ISerializableModule) module).fromNBT(tag.getCompound(module.getName()));
        });
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag)
    {
        if (descriptor != null)
            tag.putString("machineDescriptor", descriptor.getName());

        tag.putString("modid", modid);
        modules.values().forEach(module ->
        {
            if (module instanceof ISerializableModule)
                tag.put(module.getName(), ((ISerializableModule) module).toNBT(new NbtCompound()));
        });
        return super.writeNbt(tag);
    }

    @Override
    public Collection<TileModule> getModules()
    {
        return modules.values();
    }

    @Override
    public <T extends TileModule> T getModule(Class<T> moduleClass)
    {
        return (T) modules.get(moduleClass);
    }

    @Override
    public <T extends TileModule> boolean hasModule(Class<T> moduleClass)
    {
        return modules.containsKey(moduleClass);
    }

    protected void addModule(TileModule module)
    {
        modules.put(module.getClass(), module);
    }

    protected void removeModule(TileModule module)
    {
        modules.remove(module.getClass());
    }

    protected void reloadModules()
    {
        modules.clear();
    }
}

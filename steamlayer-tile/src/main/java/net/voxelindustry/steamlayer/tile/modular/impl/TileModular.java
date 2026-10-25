package net.voxelindustry.steamlayer.tile.modular.impl;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    private final HashMap<Class<? extends TileModule>, TileModule> modules;

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
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);

        modid = input.getStringOr("modid", modid);
        TileDescriptor previous = descriptor;
        descriptor = input.getString("tileDescriptor").map(ModularTiles.instance(modid)::get).orElse(descriptor);

        if (previous == null && descriptor != null)
            reloadModules();

        modules.values().forEach(module ->
        {
            if (module instanceof ISerializableModule serializableModule)
                serializableModule.load(input.childOrEmpty(module.getName()));
        });
    }

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        if (descriptor != null)
            output.putString("tileDescriptor", descriptor.getName());

        output.putString("modid", modid);
        modules.values().forEach(module ->
        {
            if (module instanceof ISerializableModule serializableModule)
                serializableModule.save(output.child(module.getName()));
        });
        super.saveAdditional(output);
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

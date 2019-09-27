package net.voxelindustry.steamlayer.tile.modular.impl;

import lombok.Getter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.voxelindustry.steamlayer.tile.TileBase;
import net.voxelindustry.steamlayer.tile.descriptor.ModularTiles;
import net.voxelindustry.steamlayer.tile.descriptor.TileDescriptor;
import net.voxelindustry.steamlayer.tile.modular.ICapabilityModule;
import net.voxelindustry.steamlayer.tile.modular.IModularTile;
import net.voxelindustry.steamlayer.tile.modular.ISerializableModule;
import net.voxelindustry.steamlayer.tile.modular.TileModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public class TileModular extends TileBase implements IModularTile
{
    private HashMap<Class<? extends TileModule>, TileModule> modules;

    @Getter
    private TileDescriptor descriptor;
    private String         modid;

    private boolean hasCapabilityModule = false;

    public TileModular(String modid, TileEntityType<? extends TileModular> type, TileDescriptor descriptor)
    {
        super(type);

        this.descriptor = descriptor;
        this.modid = modid;

        this.modules = new HashMap<>();
        if (descriptor != null)
            this.reloadModules();
    }

    public TileModular(TileEntityType<? extends TileModular> type)
    {
        this(null, type, null);
    }

    @Override
    public void read(CompoundNBT tag)
    {
        super.read(tag);

        this.modid = tag.getString("modid");
        TileDescriptor previous = this.descriptor;
        this.descriptor = ModularTiles.instance(modid).get(tag.getString("tileDescriptor"));

        if (previous == null && this.descriptor != null)
            this.reloadModules();

        modules.values().forEach(module ->
        {
            if (module instanceof ISerializableModule)
                ((ISerializableModule) module).fromNBT(tag.getCompound(module.getName()));
        });
    }

    @Override
    public CompoundNBT write(CompoundNBT tag)
    {
        if (this.descriptor != null)
            tag.putString("machineDescriptor", this.descriptor.getName());

        tag.putString("modid", this.modid);
        modules.values().forEach(module ->
        {
            if (module instanceof ISerializableModule)
                tag.put(module.getName(), ((ISerializableModule) module).toNBT(new CompoundNBT()));
        });
        return super.write(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        return this.getCapability(capability, BlockPos.ZERO, facing);
    }

    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, BlockPos from, @Nullable Direction facing)
    {
        if (this.hasCapabilityModule && this.getDescriptor() != null)
        {
            return LazyOptional.of(() ->
                    this.getModules().stream().filter(ICapabilityModule.class::isInstance)
                            .map(module -> ((ICapabilityModule) module).getCapability(capability, from, facing))
                            .filter(Objects::nonNull)
                            .findFirst().orElse(null));
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public Collection<TileModule> getModules()
    {
        return this.modules.values();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends TileModule> T getModule(Class<T> moduleClass)
    {
        return (T) this.modules.get(moduleClass);
    }

    @Override
    public <T extends TileModule> boolean hasModule(Class<T> moduleClass)
    {
        return this.modules.containsKey(moduleClass);
    }

    protected void addModule(TileModule module)
    {
        this.modules.put(module.getClass(), module);

        if (module instanceof ICapabilityModule)
            this.hasCapabilityModule = true;
    }

    protected void removeModule(TileModule module)
    {
        this.modules.remove(module.getClass());
    }

    protected void reloadModules()
    {
        this.modules.clear();
        this.hasCapabilityModule = false;
    }
}

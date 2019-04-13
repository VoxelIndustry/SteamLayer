package net.voxelindustry.steamlayer.tile.modular.impl;

import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
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
import java.util.Optional;

public class TileModular extends TileBase implements IModularTile
{
    private HashMap<Class<? extends TileModule>, TileModule> modules;

    @Getter
    private TileDescriptor descriptor;
    private String         modid;

    private boolean hasCapabilityModule = false;

    public TileModular(String modid, TileDescriptor descriptor)
    {
        this.descriptor = descriptor;
        this.modid = modid;

        this.modules = new HashMap<>();
        if (descriptor != null)
            this.reloadModules();
    }

    public TileModular()
    {
        this(null, null);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);

        this.modid = tag.getString("modid");
        TileDescriptor previous = this.descriptor;
        this.descriptor = ModularTiles.instance(modid).get(tag.getString("tileDescriptor"));

        if (previous == null && this.descriptor != null)
            this.reloadModules();

        modules.values().forEach(module ->
        {
            if (module instanceof ISerializableModule)
                ((ISerializableModule) module).fromNBT(tag.getCompoundTag(module.getName()));
        });
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        if (this.descriptor != null)
            tag.setString("machineDescriptor", this.descriptor.getName());

        tag.setString("modid", this.modid);
        modules.values().forEach(module ->
        {
            if (module instanceof ISerializableModule)
                tag.setTag(module.getName(), ((ISerializableModule) module).toNBT(new NBTTagCompound()));
        });
        return super.writeToNBT(tag);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return this.hasCapability(capability, BlockPos.ORIGIN, facing) || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        return this.getCapability(capability, BlockPos.ORIGIN, facing);
    }

    public boolean hasCapability(Capability<?> capability, BlockPos from, @Nullable EnumFacing facing)
    {
        return (this.hasCapabilityModule && this.modules.values().stream()
                .filter(module -> module instanceof ICapabilityModule)
                .anyMatch(module -> ((ICapabilityModule) module).hasCapability(capability, from, facing)));
    }

    @Nullable
    public <T> T getCapability(Capability<T> capability, BlockPos from, @Nullable EnumFacing facing)
    {
        if (this.hasCapabilityModule && this.getDescriptor() != null)
        {
            Optional<T> result =
                    this.getModules().stream().filter(ICapabilityModule.class::isInstance)
                            .map(module -> ((ICapabilityModule) module).getCapability(capability, from, facing))
                            .filter(Objects::nonNull).findFirst();
            if (result.isPresent())
                return result.get();
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

    @Nullable
    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentTranslation("gui." + this.descriptor.getName() + ".name");
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

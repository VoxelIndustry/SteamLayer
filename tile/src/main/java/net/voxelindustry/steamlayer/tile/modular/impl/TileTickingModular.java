package net.voxelindustry.steamlayer.tile.modular.impl;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Tickable;
import net.voxelindustry.steamlayer.tile.descriptor.TileDescriptor;
import net.voxelindustry.steamlayer.tile.modular.ITickableModule;
import net.voxelindustry.steamlayer.tile.modular.TileModule;

import java.util.ArrayList;
import java.util.List;

public class TileTickingModular extends TileModular implements Tickable
{
    private List<ITickableModule> tickeds;

    public TileTickingModular(String modid, BlockEntityType<? extends TileTickingModular> type, TileDescriptor descriptor)
    {
        super(modid, type, descriptor);
    }

    public TileTickingModular(BlockEntityType<? extends TileTickingModular> type)
    {
        this(null, type, null);
    }

    @Override
    public void tick()
    {
        if (getDescriptor() != null)
        {
            syncLock();
            getTickeds().forEach(ITickableModule::tick);
            releaseSyncLock(true);
        }
    }

    @Override
    protected void addModule(TileModule module)
    {
        super.addModule(module);

        if (module instanceof ITickableModule)
            getTickeds().add((ITickableModule) module);
    }

    @Override
    protected void removeModule(TileModule module)
    {
        super.removeModule(module);

        if (module instanceof ITickableModule)
            getTickeds().remove(module);
    }

    @Override
    protected void reloadModules()
    {
        getTickeds().clear();
        super.reloadModules();
    }

    private List<ITickableModule> getTickeds()
    {
        if (tickeds == null)
            tickeds = new ArrayList<>();
        return tickeds;
    }
}

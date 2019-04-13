package net.voxelindustry.steamlayer.tile.modular.impl;

import net.minecraft.util.ITickable;
import net.voxelindustry.steamlayer.tile.descriptor.TileDescriptor;
import net.voxelindustry.steamlayer.tile.modular.ITickableModule;
import net.voxelindustry.steamlayer.tile.modular.TileModule;

import java.util.ArrayList;
import java.util.List;

public class TileTickingModular extends TileModular implements ITickable
{
    private List<ITickableModule> tickeds;

    public TileTickingModular(String modid, TileDescriptor descriptor)
    {
        super(modid, descriptor);
    }

    public TileTickingModular()
    {
        this(null, null);
    }

    @Override
    public void update()
    {
        if (this.getDescriptor() != null)
        {
            this.syncLock();
            this.getTickeds().forEach(ITickableModule::tick);
            this.releaseSyncLock(true);
        }
    }

    @Override
    protected void addModule(TileModule module)
    {
        super.addModule(module);

        if (module instanceof ITickableModule)
            this.getTickeds().add((ITickableModule) module);
    }

    @Override
    protected void removeModule(TileModule module)
    {
        super.removeModule(module);

        if (module instanceof ITickableModule)
            this.getTickeds().remove(module);
    }

    @Override
    protected void reloadModules()
    {
        this.getTickeds().clear();
        super.reloadModules();
    }

    private List<ITickableModule> getTickeds()
    {
        if (this.tickeds == null)
            this.tickeds = new ArrayList<>();
        return this.tickeds;
    }
}

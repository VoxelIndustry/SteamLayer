package net.voxelindustry.steamlayer.tile.modular.impl;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.voxelindustry.steamlayer.tile.descriptor.TileDescriptor;
import net.voxelindustry.steamlayer.tile.modular.ITickableModule;
import net.voxelindustry.steamlayer.tile.modular.TileModule;

import java.util.ArrayList;
import java.util.List;

public class TileTickingModular extends TileModular
{
    private List<ITickableModule> tickeds;

    public TileTickingModular(String modid, BlockEntityType<? extends TileTickingModular> type, BlockPos pos, BlockState state, TileDescriptor descriptor)
    {
        super(modid, type, pos, state, descriptor);
    }

    public TileTickingModular(BlockEntityType<? extends TileTickingModular> type, BlockPos pos, BlockState state)
    {
        this(null, type, pos, state, null);
    }

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

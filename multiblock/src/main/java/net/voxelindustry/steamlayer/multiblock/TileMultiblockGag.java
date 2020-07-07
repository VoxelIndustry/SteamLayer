package net.voxelindustry.steamlayer.multiblock;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.voxelindustry.steamlayer.tile.TileBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileMultiblockGag extends TileBase implements ITileMultiblock
{
    public static TileEntityType<TileMultiblockGag> TYPE;

    private BlockPos corePos    = pos;
    private BlockPos coreOffset = null;

    public TileMultiblockGag(TileEntityType<? extends TileMultiblockGag> type)
    {
        super(type);
    }

    public TileMultiblockGag()
    {
        this(TileMultiblockGag.TYPE);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag)
    {
        tag.putInt("corePosX", corePos.getX());
        tag.putInt("corePosY", corePos.getY());
        tag.putInt("corePosZ", corePos.getZ());

        return super.write(tag);
    }

    @Override
    public void read(CompoundNBT tag)
    {
        corePos = new BlockPos(tag.getInt("corePosX"), tag.getInt("corePosY"), tag.getInt("corePosZ"));

        super.read(tag);
    }

    @Override
    public void onLoad()
    {
        if (isClient())
            askServerSync();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        if (isCorePresent())
            return getCore().getCapability(capability, getCoreOffset(), facing);
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean isCore()
    {
        return false;
    }

    @Override
    public boolean isCorePresent()
    {
        return world.getTileEntity(getCorePos()) != null
                && world.getTileEntity(getCorePos()) instanceof ITileMultiblockCore;
    }

    @Override
    public void breakCore()
    {
        if (isCorePresent())
            getCore().breakCore();
        world.destroyBlock(getPos(), false);
    }

    @Override
    public BlockPos getCorePos()
    {
        return corePos;
    }

    public void setCorePos(BlockPos pos)
    {
        corePos = pos;
    }

    @Override
    public ITileMultiblockCore getCore()
    {
        return (ITileMultiblockCore) world.getTileEntity(getCorePos());
    }

    @Override
    public BlockPos getCoreOffset()
    {
        if (coreOffset == null)
            coreOffset = pos.subtract(corePos);
        return coreOffset;
    }
}

package net.voxelindustry.steamlayer.multiblock;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tileentity.BlockEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.voxelindustry.steamlayer.tile.TileBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileMultiblockGag extends TileBase implements ITileMultiblock
{
    public static BlockEntityType<TileMultiblockGag> TYPE;

    private BlockPos corePos    = pos;
    private BlockPos coreOffset = null;

    public TileMultiblockGag(BlockEntityType<? extends TileMultiblockGag> type)
    {
        super(type);
    }

    public TileMultiblockGag()
    {
        this(TileMultiblockGag.TYPE);
    }

    @Override
    public CompoundTag write(CompoundTag tag)
    {
        tag.putInt("corePosX", corePos.getX());
        tag.putInt("corePosY", corePos.getY());
        tag.putInt("corePosZ", corePos.getZ());

        return super.write(tag);
    }

    @Override
    public void read(CompoundTag tag)
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
        return world.getBlockEntity(getCorePos()) != null
                && world.getBlockEntity(getCorePos()) instanceof ITileMultiblockCore;
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
        return (ITileMultiblockCore) world.getBlockEntity(getCorePos());
    }

    @Override
    public BlockPos getCoreOffset()
    {
        if (coreOffset == null)
            coreOffset = pos.subtract(corePos);
        return coreOffset;
    }
}

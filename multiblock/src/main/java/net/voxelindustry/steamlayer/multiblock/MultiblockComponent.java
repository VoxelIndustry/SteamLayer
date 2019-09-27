package net.voxelindustry.steamlayer.multiblock;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.voxelindustry.steamlayer.tile.descriptor.ITileComponent;
import net.voxelindustry.steamlayer.tile.descriptor.TileDescriptor;

import java.util.EnumMap;
import java.util.stream.Stream;

public class MultiblockComponent implements ITileComponent
{
    @Getter
    private final int width, height, length, offsetX, offsetY, offsetZ;

    private final EnumMap<Direction, BlockPos>   CORE_OFFSET;
    private final EnumMap<Direction, VoxelShape> CACHED_AABB;

    @Getter
    @Setter
    private TileDescriptor descriptor;

    public MultiblockComponent(int width, int height, int length, int offsetX, int offsetY, int offsetZ)
    {
        this.width = width;
        this.height = height;
        this.length = length;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;

        CORE_OFFSET = new EnumMap<>(Direction.class);
        CACHED_AABB = new EnumMap<>(Direction.class);

        for (Direction facing : Direction.values())
        {
            CORE_OFFSET.put(facing, internalGetCoreOffset(facing));

            if (facing.getAxis() == Axis.Z)
                CACHED_AABB.put(facing, VoxelShapes.create(
                        new AxisAlignedBB(-getOffsetX(), -getOffsetY(), -getOffsetZ(),
                                getWidth() - getOffsetX(), getHeight() - getOffsetY(),
                                getLength() - getOffsetZ()).offset(CORE_OFFSET.get(facing))));
            else
                CACHED_AABB.put(facing, VoxelShapes.create(
                        new AxisAlignedBB(-getOffsetZ(), -getOffsetY(), -getOffsetX(),
                                getLength() - getOffsetZ(), getHeight() - getOffsetY(),
                                getWidth() - getOffsetX()).offset(CORE_OFFSET.get(facing))));
        }
    }

    private BlockPos internalGetCoreOffset(Direction facing)
    {
        BlockPos rtn = BlockPos.ZERO;

        if (getLength() % 2 == 0 || getWidth() % 2 == 0)
        {
            if (getWidth() % 2 == 0 && facing.getAxis() == Axis.Z
                    && facing.getAxisDirection() == AxisDirection.NEGATIVE)
                rtn = rtn.add(-1, 0, 0);
            if (getWidth() % 2 == 0 && facing.getAxis() == Axis.X
                    && facing.getAxisDirection() == AxisDirection.POSITIVE)
                rtn = rtn.add(0, 0, -1);
            if (getLength() % 2 == 0 && facing.getAxis() == Axis.Z
                    && facing.getAxisDirection() == AxisDirection.NEGATIVE)
                rtn = rtn.add(0, 0, -1);
            if (getLength() % 2 == 0 && facing.getAxis() == Axis.X
                    && facing.getAxisDirection() == AxisDirection.NEGATIVE)
                rtn = rtn.add(-1, 0, 0);
        }
        return rtn;
    }

    public BlockPos getCoreOffset(Direction facing)
    {
        return CORE_OFFSET.get(facing);
    }

    public VoxelShape getBox(Direction facing)
    {
        return CACHED_AABB.get(facing);
    }

    public Stream<BlockPos> getAllInBox(BlockPos pos, Direction facing)
    {
        Stream<BlockPos> searchables;

        pos = pos.add(getCoreOffset(facing));
        if (facing.getAxis() == Axis.Z)
            searchables = BlockPos.getAllInBox(
                    pos.subtract(new Vec3i(getOffsetX(), getOffsetY(), getOffsetZ())),
                    pos.add(getWidth() - 1 - getOffsetX(), getHeight() - 1 - getOffsetY(),
                            getLength() - 1 - getOffsetZ()));
        else
            searchables = BlockPos.getAllInBox(
                    pos.subtract(new Vec3i(getOffsetZ(), getOffsetY(), getOffsetX())),
                    pos.add(getLength() - 1 - getOffsetZ(), getHeight() - 1 - getOffsetY(),
                            getWidth() - 1 - getOffsetX()));
        return searchables;
    }

    public MultiblockSide worldSideToMultiblockSide(MultiblockSide side, Direction orientation)
    {
        Direction resultFacing = side.getFacing();
        BlockPos resultPos = side.getPos();

        if (orientation == Direction.EAST)
        {
            if (resultFacing.getAxis().isHorizontal())
                resultFacing = resultFacing.rotateY();
            resultPos = new BlockPos(-resultPos.getZ(), resultPos.getY(), resultPos.getX());
        }
        else if (orientation == Direction.WEST)
        {
            if (resultFacing.getAxis().isHorizontal())
                resultFacing = resultFacing.rotateYCCW();
            resultPos = new BlockPos(resultPos.getZ(), resultPos.getY(), -resultPos.getX());
        }
        else if (orientation == Direction.NORTH)
        {
            if (resultFacing.getAxis().isHorizontal())
                resultFacing = resultFacing.getOpposite();
            resultPos = new BlockPos(-resultPos.getX(), resultPos.getY(), -resultPos.getZ());
        }
        return new MultiblockSide(resultPos, resultFacing);
    }

    public MultiblockSide multiblockSideToWorldSide(MultiblockSide side, Direction orientation)
    {
        Direction resultFacing = side.getFacing();
        BlockPos resultPos = side.getPos();

        if (orientation == Direction.EAST)
        {
            if (resultFacing.getAxis().isHorizontal())
                resultFacing = resultFacing.rotateYCCW();
            resultPos = new BlockPos(resultPos.getZ(), resultPos.getY(), -resultPos.getX());
        }
        else if (orientation == Direction.WEST)
        {
            if (resultFacing.getAxis().isHorizontal())
                resultFacing = resultFacing.rotateY();
            resultPos = new BlockPos(-resultPos.getZ(), resultPos.getY(), resultPos.getX());
        }
        else if (orientation == Direction.NORTH)
        {
            if (resultFacing.getAxis().isHorizontal())
                resultFacing = resultFacing.getOpposite();
            resultPos = new BlockPos(-resultPos.getX(), resultPos.getY(), -resultPos.getZ());
        }
        return new MultiblockSide(resultPos, resultFacing);
    }

    public int getBlockCount()
    {
        return width * height * length;
    }
}

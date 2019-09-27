package net.voxelindustry.steamlayer.grid;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.EnumMap;

public interface ITileCable<T extends CableGrid> extends ITileNode<T>
{
    EnumMap<Direction, ITileCable<T>> getConnectionsMap();

    default void adjacentConnect()
    {
        for (final Direction facing : Direction.values())
        {
            final TileEntity adjacent = this.getBlockWorld().getTileEntity(this.getAdjacentPos(facing));
            if (adjacent instanceof ITileCable && this.canConnect(facing, (ITileCable<?>) adjacent)
                    && ((ITileCable<?>) adjacent).canConnect(facing.getOpposite(), this))
            {
                this.connect(facing, (ITileCable<T>) adjacent);
                ((ITileCable<T>) adjacent).connect(facing.getOpposite(), this);
                ((ITileCable<T>) adjacent).updateState();
            }
        }
        this.updateState();
    }

    boolean canConnect(Direction facing, ITileNode<?> to);

    default BlockPos getAdjacentPos(final Direction facing)
    {
        return this.getBlockPos().offset(facing);
    }

    default int[] getConnections()
    {
        return this.getConnectionsMap().keySet().stream().mapToInt(Direction::ordinal).toArray();
    }

    default ITileCable<T> getConnected(int edge)
    {
        return this.getConnected(Direction.values()[edge]);
    }

    default ITileCable<T> getConnected(Direction facing)
    {
        return this.getConnectionsMap().get(facing);
    }

    default void connect(Direction facing, ITileCable<T> to)
    {
        this.getConnectionsMap().put(facing, to);
    }

    default void disconnect(Direction facing)
    {
        this.getConnectionsMap().remove(facing);
        this.updateState();
    }

    @Override
    default void disconnect(int edge)
    {
        this.disconnect(Direction.values()[edge]);
    }

    @Override
    default int invertEdge(int edge)
    {
        return Direction.values()[edge].getOpposite().ordinal();
    }

    @Override
    default boolean canConnect(int edge, ITileNode<?> to)
    {
        return this.canConnect(Direction.values()[edge], to);
    }
}

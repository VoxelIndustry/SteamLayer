package net.voxelindustry.steamlayer.grid;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.EnumMap;

public interface ITileCable<T extends CableGrid> extends ITileNode<T>
{
    EnumMap<Direction, ITileCable<T>> getConnectionsMap();

    @Override
    default void adjacentConnect()
    {
        for (Direction facing : Direction.values())
        {
            TileEntity adjacent = getBlockWorld().getTileEntity(getAdjacentPos(facing));
            if (adjacent instanceof ITileCable && canConnect(facing, (ITileCable<?>) adjacent)
                    && ((ITileCable<?>) adjacent).canConnect(facing.getOpposite(), this))
            {
                connect(facing, (ITileCable<T>) adjacent);
                ((ITileCable<T>) adjacent).connect(facing.getOpposite(), this);
                ((ITileCable<T>) adjacent).updateState();
            }
        }
        updateState();
    }

    boolean canConnect(Direction facing, ITileNode<?> to);

    default BlockPos getAdjacentPos(Direction facing)
    {
        return getBlockPos().offset(facing);
    }

    @Override
    default int[] getConnections()
    {
        return getConnectionsMap().keySet().stream().mapToInt(Direction::ordinal).toArray();
    }

    @Override
    default ITileCable<T> getConnected(int edge)
    {
        return getConnected(Direction.values()[edge]);
    }

    default ITileCable<T> getConnected(Direction facing)
    {
        return getConnectionsMap().get(facing);
    }

    default void connect(Direction facing, ITileCable<T> to)
    {
        getConnectionsMap().put(facing, to);
    }

    default void disconnect(Direction facing)
    {
        getConnectionsMap().remove(facing);
        updateState();
    }

    default void disconnectSelf()
    {
        getGridManager().disconnectCable(this);
    }

    @Override
    default void disconnect(int edge)
    {
        disconnect(Direction.values()[edge]);
    }

    @Override
    default int invertEdge(int edge)
    {
        return Direction.values()[edge].getOpposite().ordinal();
    }

    @Override
    default boolean canConnect(int edge, ITileNode<?> to)
    {
        return canConnect(Direction.values()[edge], to);
    }
}

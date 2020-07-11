package net.voxelindustry.steamlayer.grid;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.EnumMap;

public class ITileCableTestImpl implements ITileCable
{
    private static final GridManager GRID_MANAGER = GridManager.createGetInstance("steamlayer:test");
    private              int         grid         = -1;

    private EnumMap<Direction, ITileCable> connecteds = new EnumMap<>(Direction.class);

    @Override
    public BlockPos getBlockPos()
    {
        return null;
    }

    @Override
    public int[] getConnections()
    {
        return connecteds.keySet().stream().mapToInt(Direction::ordinal).toArray();
    }

    @Override
    public ITileCable<?> getConnected(Direction facing)
    {
        return connecteds.get(facing);
    }

    @Override
    public int getGrid()
    {
        return grid;
    }

    @Override
    public void setGrid(int gridIdentifier)
    {
        grid = gridIdentifier;
    }

    @Override
    public boolean canConnect(Direction facing, ITileNode to)
    {
        return true;
    }

    @Override
    public void connect(Direction facing, ITileCable to)
    {
        connecteds.put(facing, to);
    }

    @Override
    public void disconnect(Direction facing)
    {
        connecteds.remove(facing);
    }

    @Override
    public World getBlockWorld()
    {
        return null;
    }

    @Override
    public CableGrid createGrid(int nextID)
    {
        return null;
    }

    @Override
    public GridManager getGridManager()
    {
        return GRID_MANAGER;
    }

    @Override
    public EnumMap<Direction, ITileCable> getConnectionsMap()
    {
        return connecteds;
    }

    @Override
    public void updateState()
    {

    }

    @Override
    public void adjacentConnect()
    {

    }
}

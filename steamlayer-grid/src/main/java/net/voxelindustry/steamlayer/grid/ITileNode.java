package net.voxelindustry.steamlayer.grid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ITileNode<T extends CableGrid>
{
    BlockPos getBlockPos();

    int getGrid();

    void setGrid(int gridIdentifier);

    boolean canConnect(int edge, ITileNode<?> to);

    Level getBlockWorld();

    T createGrid(int nextID);

    GridManager getGridManager();

    default T getGridObject()
    {
        try
        {
            T grid = (T) getGridManager().getGrid(getGrid());

            if (grid != null)
                return grid;
        } catch (ClassCastException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    default void updateState()
    {

    }

    void adjacentConnect();

    default boolean hasGrid()
    {
        return getGrid() != -1;
    }

    int[] getConnections();

    ITileNode<T> getConnected(int edge);

    void disconnect(int edge);

    int invertEdge(int edge);
}
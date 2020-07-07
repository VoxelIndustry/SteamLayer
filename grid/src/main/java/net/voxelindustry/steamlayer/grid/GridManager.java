package net.voxelindustry.steamlayer.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GridManager
{
    private static Map<String, GridManager> instances = new HashMap<>();

    public static GridManager createGetInstance(String modid)
    {
        if (!instances.containsKey(modid))
            instances.put(modid, new GridManager());
        return instances.get(modid);
    }

    public static void onServerShutdown()
    {
        for (GridManager manager : instances.values())
        {
            for (CableGrid grid : manager.gridsByIdentifier.values())
                grid.getCables().clear();
            manager.gridsByIdentifier.clear();
        }
    }

    public final Map<Integer, CableGrid> gridsByIdentifier = new ConcurrentHashMap<>();

    private final List<Integer>      gridsToRemove = new ArrayList<>();
    private final List<ITickingGrid> tickingGrids  = new ArrayList<>();

    private GridManager()
    {
    }

    public CableGrid addGrid(CableGrid grid)
    {
        if (!gridsByIdentifier.containsKey(grid.getIdentifier()))
            gridsByIdentifier.put(grid.getIdentifier(), grid);
        grid.setGridManager(this);

        if (grid instanceof ITickingGrid)
            tickingGrids.add((ITickingGrid) grid);
        return grid;
    }

    public void markGridForRemoval(CableGrid grid)
    {
        gridsToRemove.add(grid.getIdentifier());
    }

    public CableGrid removeGrid(int identifier)
    {
        CableGrid grid = gridsByIdentifier.get(identifier);

        if (grid instanceof ITickingGrid)
            tickingGrids.remove(grid);

        return gridsByIdentifier.remove(identifier);
    }

    public boolean hasGrid(int identifier)
    {
        return gridsByIdentifier.containsKey(identifier);
    }

    public CableGrid getGrid(int identifier)
    {
        if (gridsByIdentifier.containsKey(identifier))
            return gridsByIdentifier.get(identifier);
        return null;
    }

    public int getNextID()
    {
        int i = 0;
        while (gridsByIdentifier.containsKey(i))
            i++;
        return i;
    }

    public void tickGrids(int tickIndex)
    {
        gridsToRemove.forEach(gridsByIdentifier::remove);
        gridsToRemove.clear();


        tickingGrids.forEach(grid ->
        {
            if (tickIndex % (20 / grid.getTickRate()) == 0) // Implicit floor is intended -> 20 / 3 per seconds = 6.667 = 6 -> Always 3 times per 20 ticks
            {
                grid.tick(tickIndex);
            }
        });
    }

    public <T extends CableGrid> void connectCable(ITileNode<T> added)
    {
        added.adjacentConnect();

        for (int edge : added.getConnections())
        {
            ITileNode<T> adjacent = added.getConnected(edge);

            if (adjacent.getGrid() != -1)
            {
                if (added.getGrid() == -1 && getGrid(adjacent.getGrid()) != null)
                {
                    added.setGrid(adjacent.getGrid());
                    getGrid(adjacent.getGrid()).addCable(added);
                }
                else if (getGrid(added.getGrid()).canMerge(getGrid(adjacent.getGrid())))
                    mergeGrids(getGrid(added.getGrid()), getGrid(adjacent.getGrid()));
            }
        }

        if (added.getGrid() == -1)
        {
            CableGrid newGrid = addGrid(added.createGrid(getNextID()));
            newGrid.addCable(added);
            added.setGrid(newGrid.getIdentifier());
        }
    }

    public <T extends CableGrid> void disconnectCable(ITileNode<T> removed)
    {
        if (removed.getGrid() != -1)
        {
            if (removed.getConnections().length != 0)
            {
                for (int edge : removed.getConnections())
                    removed.getConnected(edge).disconnect(removed.invertEdge(edge));

                if (removed.getConnections().length == 1)
                {
                    getGrid(removed.getGrid()).removeCable(removed);
                    removed.setGrid(-1);
                }
                else
                {
                    getGrid(removed.getGrid()).removeCable(removed);
                    if (!getOrphans(getGrid(removed.getGrid()), removed).isEmpty())
                    {
                        for (int edge : removed.getConnections())
                            removed.getConnected(edge).setGrid(-1);
                        CableGrid old = removeGrid(removed.getGrid());
                        for (int edge : removed.getConnections())
                        {
                            if (removed.getConnected(edge).getGrid() == -1)
                            {
                                CableGrid newGrid = addGrid(old.copy(getNextID()));

                                exploreGrid(newGrid, removed.getConnected(edge));
                                newGrid.onSplit(old);
                            }
                        }
                    }
                    removed.setGrid(-1);
                }
            }
            else
                removeGrid(removed.getGrid());
        }
    }

    public void mergeGrids(CableGrid destination, CableGrid source)
    {
        destination.addCables(source.getCables());

        source.getCables().forEach(cable -> cable.setGrid(destination.getIdentifier()));
        gridsByIdentifier.remove(source.getIdentifier());
        destination.onMerge(source);
    }

    public <T extends CableGrid> List<ITileNode<T>> getOrphans(CableGrid grid, ITileNode<T> cable)
    {
        List<ITileNode<T>> toScan = new ArrayList<>();
        // Shut the fuck up gradle
        grid.getCables().forEach(cable2 -> toScan.add((ITileNode<T>) cable2));

        List<ITileNode<T>> openset = new ArrayList<>();
        List<ITileNode<T>> frontier = new ArrayList<>();

        frontier.add(cable.getConnected(cable.getConnections()[0]));
        while (!frontier.isEmpty())
        {
            List<ITileNode<T>> frontierCpy = new ArrayList<>(frontier);
            for (ITileNode<T> current : frontierCpy)
            {
                openset.add(current);
                toScan.remove(current);
                for (int edge : current.getConnections())
                {
                    ITileNode<T> facingCable = current.getConnected(edge);
                    if (!openset.contains(facingCable) && !frontier.contains(facingCable))
                        frontier.add(facingCable);
                }
                frontier.remove(current);
            }
        }
        return toScan;
    }

    private <T extends CableGrid> void exploreGrid(CableGrid grid, ITileNode<T> cable)
    {
        Set<ITileNode<T>> openset = new HashSet<>();
        Set<ITileNode<T>> frontier = new HashSet<>();

        frontier.add(cable);
        while (!frontier.isEmpty())
        {
            Set<ITileNode<T>> frontierCpy = new HashSet<>(frontier);
            for (ITileNode<T> current : frontierCpy)
            {
                openset.add(current);
                current.setGrid(grid.getIdentifier());
                grid.addCable(current);
                for (int edge : current.getConnections())
                {
                    ITileNode<T> facingCable = current.getConnected(edge);

                    if (!openset.contains(facingCable))
                        frontier.add(facingCable);
                }
                frontier.remove(current);
            }
        }
    }
}

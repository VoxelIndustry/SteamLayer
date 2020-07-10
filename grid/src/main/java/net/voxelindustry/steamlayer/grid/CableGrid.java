package net.voxelindustry.steamlayer.grid;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.HashSet;

@EqualsAndHashCode
@ToString
@Getter
public abstract class CableGrid
{
    private final int identifier;

    private final HashSet<ITileNode<?>> cables;

    @Setter(AccessLevel.PACKAGE)
    @EqualsAndHashCode.Exclude
    private GridManager gridManager;

    public CableGrid(int identifier)
    {
        this.identifier = identifier;

        cables = new HashSet<>();
    }

    public abstract CableGrid copy(int identifier);

    public boolean canMerge(CableGrid grid)
    {
        return grid.getIdentifier() != getIdentifier();
    }

    /**
     * Called on the destination grid after the merging has occurred.
     *
     * @param grid the source grid
     */
    public void onMerge(CableGrid grid)
    {

    }

    /**
     * Called after a grid splitting has occurred, each new fragment will
     * receive this event.
     *
     * @param grid the grid source grid before splitting.
     */
    public void onSplit(CableGrid grid)
    {

    }

    public void addCable(ITileNode<?> cable)
    {
        cables.add(cable);
    }

    public void addCables(Collection<ITileNode<?>> cables)
    {
        cables.forEach(this::addCable);
    }

    public boolean removeCable(ITileNode<?> cable)
    {
        if (cables.remove(cable))
        {
            if (cables.isEmpty())
                getGridManager().markGridForRemoval(this);
            return true;
        }
        return false;
    }

    public void removeCables(Collection<ITileNode<?>> cables)
    {
        cables.forEach(this::removeCable);
    }

    public boolean hasCable(ITileNode<?> cable)
    {
        return cables.contains(cable);
    }

    public boolean isEmpty()
    {
        return getCables().isEmpty();
    }

    public ITileNode<?> getFromPos(BlockPos pos)
    {
        return cables.stream().filter(node -> node.getBlockPos().equals(pos)).findFirst().orElse(null);
    }
}

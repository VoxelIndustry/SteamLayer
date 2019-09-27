package net.voxelindustry.steamlayer.grid;

import net.minecraft.util.Direction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GridManagerTest
{
    private GridManager instance;

    @Before
    public void setupTest()
    {
        instance = GridManager.createGetInstance("steamlayer:test");
        instance.cableGrids.clear();
    }

    @Test
    public void testGridList()
    {
        CableGrid grid = new CableGrid(instance.getNextID())
        {
            @Override
            public CableGrid copy(int identifier)
            {
                return null;
            }
        };
        instance.addGrid(grid);

        assertThat(grid.getIdentifier()).isEqualTo(0);
        assertThat(instance.getNextID()).isEqualTo(1);
        assertThat(instance.hasGrid(0)).isTrue();
        assertThat(instance.getGrid(0)).isEqualTo(grid);
        assertThat(instance.getGrid(1)).isNull();
    }

    @Test
    public void testGridAutoRemoval()
    {
        CableGrid grid = new CableGrid(instance.getNextID())
        {
            @Override
            public CableGrid copy(int identifier)
            {
                return null;
            }
        };
        instance.addGrid(grid);

        ITileCable cable = mock(ITileCable.class);
        grid.addCable(cable);

        assertThat(instance.cableGrids).containsValue(grid);

        grid.removeCable(cable);
        instance.tickGrids();

        assertThat(instance.cableGrids).doesNotContainValue(grid);
    }

    @Test
    public void testGridTick()
    {
        CableGrid grid = mock(CableGrid.class);
        when(grid.getIdentifier()).thenReturn(0);

        instance.addGrid(grid);
        instance.tickGrids();
        verify(grid, times(1)).tick();
    }

    @Test
    public void testCableConnect()
    {
        ITileCable upperCable = mock(ITileCable.class);

        ITileCable cable = mock(ITileCable.class);
        when(cable.getGrid()).thenReturn(-1);
        when(cable.getConnections()).thenReturn(new int[0]);
        when(cable.createGrid(anyInt())).then(answer -> new CableGrid(answer.getArgument(0))
        {
            @Override
            public CableGrid copy(int identifier)
            {
                return null;
            }
        });

        instance.connectCable(cable);
        assertThat(instance.cableGrids).hasSize(1);

        instance.getGrid(0).addCable(upperCable);
        instance.getGrid(0).removeCable(cable);
        when(cable.getConnections()).thenReturn(new int[]{Direction.UP.ordinal()});
        when(cable.getConnected(Direction.UP.ordinal())).thenReturn(upperCable);
        when(upperCable.getGrid()).thenReturn(0);

        instance.connectCable(cable);

        assertThat(instance.getGrid(0).getCables()).hasSize(2);
        assertThat(instance.getGrid(0).getCables()).contains(cable, upperCable);
    }

    @Test
    public void testCableMerge()
    {
        ITileCable center = spy(ITileCableTestImpl.class);
        ITileCable left = spy(ITileCableTestImpl.class);
        ITileCable right = spy(ITileCableTestImpl.class);

        when(center.getConnections()).thenReturn(new int[]{Direction.WEST.ordinal(), Direction.EAST.ordinal()});
        when(center.getConnected(Direction.WEST)).thenReturn(left);
        when(center.getConnected(Direction.EAST)).thenReturn(right);

        CableGrid leftGrid = new CableGrid(0)
        {
            @Override
            public CableGrid copy(int identifier)
            {
                return null;
            }
        };

        CableGrid rightGrid = new CableGrid(1)
        {
            @Override
            public CableGrid copy(int identifier)
            {
                return null;
            }
        };

        instance.addGrid(leftGrid);
        instance.addGrid(rightGrid);

        leftGrid.addCable(left);
        left.setGrid(leftGrid.getIdentifier());
        rightGrid.addCable(right);
        right.setGrid(rightGrid.getIdentifier());

        instance.connectCable(center);

        assertThat(instance.cableGrids).hasSize(1);
        assertThat(left.getGrid()).isEqualTo(center.getGrid());
        assertThat(right.getGrid()).isEqualTo(center.getGrid());
    }

    @Test
    public void testCableDisconnect()
    {
        ITileCable cable = spy(ITileCableTestImpl.class);

        cable.setGrid(instance.addGrid(new CableGrid(0)
        {
            @Override
            public CableGrid copy(int identifier)
            {
                return null;
            }
        }).getIdentifier());

        instance.disconnectCable(cable);

        assertThat(instance.cableGrids).isEmpty();

        ITileCable neighbor = spy(ITileCableTestImpl.class);

        CableGrid grid = instance.addGrid(new CableGrid(0)
        {
            @Override
            public CableGrid copy(int identifier)
            {
                return null;
            }
        });

        grid.addCable(cable);
        grid.addCable(neighbor);
        cable.setGrid(0);
        neighbor.setGrid(0);

        when(cable.getConnections()).thenReturn(new int[]{Direction.UP.ordinal()});
        when(cable.getConnected(Direction.UP)).thenReturn(neighbor);

        instance.disconnectCable(cable);

        assertThat(instance.cableGrids).hasSize(1);
        verify(neighbor, times(1)).disconnect(Direction.DOWN.ordinal());
    }

    @Test
    public void testGridSplit()
    {
        ITileCable center = spy(ITileCableTestImpl.class);
        ITileCable left = spy(ITileCableTestImpl.class);
        ITileCable right = spy(ITileCableTestImpl.class);
        ITileCable rightDangling = spy(ITileCableTestImpl.class);

        center.connect(Direction.WEST, left);
        center.connect(Direction.EAST, right);

        left.connect(Direction.EAST, center);
        right.connect(Direction.WEST, center);
        right.connect(Direction.UP, rightDangling);

        CableGrid grid = new CableGridTestImpl(0);

        instance.addGrid(grid);

        grid.addCable(left);
        left.setGrid(grid.getIdentifier());
        grid.addCable(right);
        right.setGrid(grid.getIdentifier());
        grid.addCable(center);
        center.setGrid(grid.getIdentifier());
        grid.addCable(rightDangling);
        rightDangling.setGrid(grid.getIdentifier());

        instance.disconnectCable(center);

        assertThat(instance.cableGrids).hasSize(2);
        assertThat(left.getGrid()).isNotEqualTo(right.getGrid());
        assertThat(right.getGrid()).isEqualTo(rightDangling.getGrid());
    }

    @Test
    public void testLoopSplit()
    {
        ITileCable north = new ITileCableTestImpl();
        ITileCable south = new ITileCableTestImpl();
        ITileCable east = new ITileCableTestImpl();
        ITileCable west = new ITileCableTestImpl();

        ITileCable northeast = new ITileCableTestImpl();
        ITileCable northwest = new ITileCableTestImpl();
        ITileCable southeast = new ITileCableTestImpl();
        ITileCable southwest = new ITileCableTestImpl();

        north.connect(Direction.EAST, northeast);
        north.connect(Direction.WEST, northwest);
        northeast.connect(Direction.WEST, north);
        northwest.connect(Direction.EAST, north);

        south.connect(Direction.EAST, southeast);
        south.connect(Direction.WEST, southwest);
        southeast.connect(Direction.WEST, south);
        southwest.connect(Direction.EAST, south);

        east.connect(Direction.NORTH, northeast);
        east.connect(Direction.SOUTH, southeast);
        northeast.connect(Direction.SOUTH, east);
        southeast.connect(Direction.NORTH, east);

        west.connect(Direction.NORTH, northwest);
        west.connect(Direction.SOUTH, southwest);
        northwest.connect(Direction.SOUTH, west);
        southwest.connect(Direction.NORTH, west);

        CableGrid grid = new CableGridTestImpl(0);

        instance.addGrid(grid);
        grid.addCables(Arrays.asList(north, south, east, west, northeast, northwest, southeast, southwest));
        grid.getCables().forEach(cable -> cable.setGrid(grid.getIdentifier()));

        instance.disconnectCable(north);

        assertThat(instance.cableGrids).hasSize(1);
        assertThat(grid.getCables()).hasSize(7);
        assertThat(south.getGrid()).isEqualTo(east.getGrid());
        assertThat(east.getGrid()).isEqualTo(west.getGrid());
        assertThat(west.getGrid()).isEqualTo(northwest.getGrid());
        assertThat(northwest.getGrid()).isEqualTo(southwest.getGrid());
        assertThat(southwest.getGrid()).isEqualTo(southeast.getGrid());
    }
}

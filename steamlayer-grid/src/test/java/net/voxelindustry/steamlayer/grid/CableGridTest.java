package net.voxelindustry.steamlayer.grid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CableGridTest
{
    private GridManager instance;

    @BeforeEach
    public void setupTest()
    {
        instance = GridManager.createGetInstance("steamlayer:test");
        instance.gridsByIdentifier.clear();
    }

    @Test
    public void testCableHandling()
    {
        CableGrid grid = new CableGridTestImpl(0);
        instance.addGrid(grid);
        ITileCable cable = new ITileCableTestImpl();

        grid.addCable(cable);

        assertThat(grid.hasCable(cable)).isTrue();
        assertThat(grid.removeCable(cable)).isTrue();
        assertThat(grid.removeCable(cable)).isFalse();
    }

    @Test
    public void testBasicGrid()
    {
        CableGrid grid = new CableGridTestImpl(0);
        instance.addGrid(grid);
        CableGrid copy = grid.copy(0);

        assertThat(grid.canMerge(grid)).isFalse();
        assertThat(grid.equals(copy)).isTrue();
        assertThat(grid.equals(null)).isFalse();
        assertThat(grid.equals(0)).isFalse();
    }
}

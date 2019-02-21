package net.voxelindustry.steamlayer.grid;

public class CableGridTestImpl extends CableGrid
{
    CableGridTestImpl(int identifier)
    {
        super(identifier);
    }

    @Override
    public CableGrid copy(int identifier)
    {
        return new CableGridTestImpl(identifier);
    }
}

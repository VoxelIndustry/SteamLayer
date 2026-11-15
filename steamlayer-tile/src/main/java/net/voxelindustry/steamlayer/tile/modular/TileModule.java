package net.voxelindustry.steamlayer.tile.modular;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.voxelindustry.steamlayer.tile.TileBase;

@Getter
@AllArgsConstructor
public abstract class TileModule
{
    private IModularTile machine;
    private String       name;

    public <T extends TileBase & IModularTile> T getModularTile()
    {
        return (T) this.machine;
    }

    public boolean isClient()
    {
        return this.getModularTile().isClient();
    }

    public boolean isServer()
    {
        return this.getModularTile().isServer();
    }

    // TODO : Enhance the sync system with a per module sync control to limit network overhead
    public void sync()
    {
        this.getModularTile().sync();
    }

    public void forceSync()
    {
        this.getModularTile().askServerSync();
    }
}

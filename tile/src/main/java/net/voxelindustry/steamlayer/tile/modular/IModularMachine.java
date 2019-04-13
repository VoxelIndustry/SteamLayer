package net.voxelindustry.steamlayer.tile.modular;

import net.minecraft.util.EnumFacing;
import net.ros.common.machine.MachineDescriptor;
import org.yggard.hermod.IEventEmitter;

import java.util.Collection;

public interface IModularMachine extends IEventEmitter
{
    Collection<TileModule> getModules();

    <T extends TileModule> T getModule(Class<T> moduleClass);

    <T extends TileModule> boolean hasModule(Class<T> moduleClass);

    MachineDescriptor getDescriptor();

    EnumFacing getFacing();
}

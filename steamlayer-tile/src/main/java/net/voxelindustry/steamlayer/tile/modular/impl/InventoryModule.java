package net.voxelindustry.steamlayer.tile.modular.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.voxelindustry.steamlayer.inventory.InventoryHandler;
import net.voxelindustry.steamlayer.tile.modular.IModularTile;
import net.voxelindustry.steamlayer.tile.modular.ISerializableModule;
import net.voxelindustry.steamlayer.tile.modular.TileModule;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;

public class InventoryModule extends TileModule implements ISerializableModule
{
    private final Map<String, InventoryHandler> inventories;
    private final Map<String, IntConsumer>      onSlotChangeEvents;

    public InventoryModule(IModularTile machine, int slotCount)
    {
        super(machine, "InventoryModule");

        inventories = new HashMap<>();
        onSlotChangeEvents = new HashMap<>();

        if (slotCount != -1)
            addBasic(slotCount);
    }

    public InventoryModule(IModularTile machine)
    {
        this(machine, -1);
    }

    @Override
    public void load(ValueInput input)
    {
        inventories.forEach((name, inv) -> inv.load(input.childOrEmpty("Inv" + name)));
    }

    @Override
    public void save(ValueOutput output)
    {
        inventories.forEach((name, inv) -> inv.save(output.child("Inv" + name)));
    }

    public void addBasic(int slotCount)
    {
        addInventory("basic", new InventoryHandler(slotCount));
    }

    public InventoryModule addInventory(String name, InventoryHandler inventory)
    {
        inventories.put(name, inventory);

        inventory.onSlotChange(slot ->
        {
            getModularTile().setChanged();

            if (onSlotChangeEvents.containsKey(name) && slot != -1)
                onSlotChangeEvents.get(name).accept(slot);
        });
        return this;
    }

    public void setOnSlotChange(String name, IntConsumer onSlotChange)
    {
        onSlotChangeEvents.put(name, onSlotChange);
    }

    public InventoryHandler getInventory(String name)
    {
        return inventories.get(name);
    }

    public boolean hasInventory(String name)
    {
        return inventories.containsKey(name);
    }

    public void dropAll(Level world, BlockPos pos)
    {
        for (InventoryHandler inventory : inventories.values())
        {
            if (inventory.canDropContents())
            {
                Containers.dropContents(world, pos, inventory);
            }
        }
    }
}

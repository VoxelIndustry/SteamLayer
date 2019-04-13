package net.voxelindustry.steamlayer.tile.modular.impl;

import lombok.Getter;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
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

    @Getter
    private CombinedInvWrapper combinedInventory;

    public InventoryModule(IModularTile machine, int slotCount)
    {
        super(machine, "InventoryModule");

        this.inventories = new HashMap<>();
        this.onSlotChangeEvents = new HashMap<>();

        if (slotCount != -1)
            this.addBasic(slotCount);
    }

    public InventoryModule(IModularTile machine)
    {
        this(machine, -1);
    }

    @Override
    public void fromNBT(NBTTagCompound tag)
    {
        this.inventories.forEach((name, inv) -> inv.deserializeNBT(tag.getCompoundTag("Inv" + name)));
    }

    @Override
    public NBTTagCompound toNBT(NBTTagCompound tag)
    {
        this.inventories.forEach((name, inv) -> tag.setTag("Inv" + name, inv.serializeNBT()));
        return tag;
    }

    public void addBasic(int slotCount)
    {
        this.addInventory("basic", new InventoryHandler(slotCount));
    }

    public InventoryModule addInventory(String name, InventoryHandler inventory)
    {
        this.inventories.put(name, inventory);

        inventory.setOnSlotChange(slot ->
        {
            this.getModularTile().markDirty();

            if (this.onSlotChangeEvents.containsKey(name) && slot != -1)
                this.onSlotChangeEvents.get(name).accept(slot);
        });

        this.combinedInventory = new CombinedInvWrapper(this.inventories.values().toArray(new InventoryHandler[0]));
        return this;
    }

    public void setOnSlotChange(String name, IntConsumer onSlotChange)
    {
        this.onSlotChangeEvents.put(name, onSlotChange);
    }

    public InventoryHandler getInventory(String name)
    {
        return this.inventories.get(name);
    }

    public boolean hasInventory(String name)
    {
        return this.inventories.containsKey(name);
    }

    public void dropAll(World world, BlockPos pos)
    {
        for (InventoryHandler inventory : this.inventories.values())
        {
            if (inventory.canDropContents())
                for (int slot = 0; slot < inventory.getSlots(); slot++)
                    InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(),
                            inventory.getStackInSlot(slot));
        }
    }
}

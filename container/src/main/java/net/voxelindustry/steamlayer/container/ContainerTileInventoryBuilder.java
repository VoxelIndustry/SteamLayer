package net.voxelindustry.steamlayer.container;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.voxelindustry.steamlayer.container.slot.FilteredSlot;
import net.voxelindustry.steamlayer.container.slot.ListenerSlot;
import net.voxelindustry.steamlayer.container.slot.SlotDisplay;
import net.voxelindustry.steamlayer.container.slot.SlotFuel;
import net.voxelindustry.steamlayer.container.slot.SlotOutput;
import org.apache.commons.lang3.Range;

import java.util.function.Predicate;

public class ContainerTileInventoryBuilder
{
    private final ItemStackHandler inventory;
    private final ContainerBuilder parent;
    private final int              rangeStart;

    ContainerTileInventoryBuilder(ContainerBuilder parent, ItemStackHandler inventory)
    {
        this.inventory = inventory;
        this.parent = parent;
        rangeStart = parent.slots.size();
    }

    /**
     * Utility method to add a line of filtered slots.
     *
     * @param indexStart the slot index at which the line begins
     * @param x          the horizontal position at which the line begins
     * @param y          the vertical position at which the line begins
     * @param quantity   the number of slot to be added
     * @param axis       the {@link Direction.Axis} to follow
     * @param filter     the {@link ItemStack} predicate to use for filtering logic
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerTileInventoryBuilder filterSlotLine(int indexStart, int x, int y, int quantity,
                                                        Direction.Axis axis, Predicate<ItemStack> filter)
    {
        for (int i = 0; i < quantity; i++)
        {
            if (axis.isHorizontal())
                filterSlot(indexStart + i, x + (i * 18), y, filter);
            else
                filterSlot(indexStart + i, x, y + (i * 18), filter);
        }
        return this;
    }

    /**
     * Utility method to add a line of slots.
     *
     * @param indexStart the slot index at which the line begins
     * @param x          the horizontal position at which the line begins
     * @param y          the vertical position at which the line begins
     * @param quantity   the number of slot to be added
     * @param axis       the {@link Direction.Axis} to follow
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerTileInventoryBuilder slotLine(int indexStart, int x, int y, int quantity, Direction.Axis axis)
    {
        for (int i = 0; i < quantity; i++)
        {
            if (axis.isHorizontal())
                slot(indexStart + i, x + (i * 18), y);
            else
                slot(indexStart + i, x, y + (i * 18));
        }
        return this;
    }

    /**
     * Add a default slot to the slot list of the current {@code IInventory}.
     *
     * @param index the index this slot will use to communicate with the inventory.
     * @param x     the horizontal position at which the slot is placed
     * @param y     the vertical position at which the slot is placed
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerTileInventoryBuilder slot(int index, int x, int y)
    {
        parent.slots.add(new ListenerSlot(inventory, index, x, y));
        return this;
    }

    /**
     * Add an output slot to the slot list of the current {@code IInventory}.
     * This slot will be give-only, stacks cannot be manually placed inside.
     *
     * @param index the index this slot will use to communicate with the inventory.
     * @param x     the horizontal position at which the slot is placed
     * @param y     the vertical position at which the slot is placed
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerTileInventoryBuilder outputSlot(int index, int x, int y)
    {
        parent.slots.add(new SlotOutput(inventory, index, x, y));
        return this;
    }

    /**
     * Add a filter slot to the slot list of the current {@code IInventory}.
     * This slot will be a special filtered slot that only accept stacks matching a specified {@link ItemStack}
     * predicate.
     *
     * @param index  the index this slot will use to communicate with the inventory.
     * @param x      the horizontal position at which the slot is placed
     * @param y      the vertical position at which the slot is placed
     * @param filter a predicate using the current {@link ItemStack} queried for the filter logic
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerTileInventoryBuilder filterSlot(int index, int x, int y,
                                                    Predicate<ItemStack> filter)
    {
        parent.slots.add(new FilteredSlot(inventory, index, x, y).setFilter(filter));
        return this;
    }

    /**
     * Add a fluid-containers-only slot to the slot list of the current {@code IInventory}
     * <p>
     * The builtin filter will only allow {@link ItemStack} having the {@code CapabilityFluidHandler
     * .FLUID_HANDLER_ITEM_CAPABILITY} with {@code Direction.UP}
     *
     * @param index the index this slot will use to communicate with the inventory.
     * @param x     the horizontal position at which the slot is placed
     * @param y     the vertical position at which the slot is placed
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerTileInventoryBuilder fluidSlot(int index, int x, int y)
    {
        parent.slots.add(new FilteredSlot(inventory, index, x, y).setFilter(
                stack -> stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, Direction.UP).isPresent()));
        return this;
    }

    /**
     * Add a fuel-only slot to the slot list of the current {@code IInventory}
     * <p>
     * The builtin filter will query {@link net.minecraft.tileentity.FurnaceTileEntity#isFuel(ItemStack)} (ItemStack)} with
     * the exception of allowing buckets for vanilla behavior compatibility.
     *
     * @param index the index this slot will use to communicate with the inventory.
     * @param x     the horizontal position at which the slot is placed
     * @param y     the vertical position at which the slot is placed
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerTileInventoryBuilder fuelSlot(int index, int x, int y)
    {
        parent.slots.add(new SlotFuel(inventory, index, x, y));
        return this;
    }

    /**
     * Add a display only slot to the slot list of the current {@code IInventory}
     * <p>
     * ItemStacks are not allowed to be taken or deposited in this slot.
     * The contained ItemStack is guaranteed to be immutable to player interaction.
     *
     * @param index the index this slot will use to communicate with the inventory.
     * @param x     the horizontal position at which the slot is placed
     * @param y     the vertical position at which the slot is placed
     * @return a reference to this {@code ContainerTileInventoryBuilder} to resume the "Builder" pattern
     */
    public ContainerTileInventoryBuilder displaySlot(int index, int x, int y)
    {
        parent.slots.add(new SlotDisplay(inventory, index, x, y));
        return this;
    }

    /**
     * Close this builder and add the slot list to the current {@link BuiltContainer} construction.
     *
     * @return the parent {@link ContainerBuilder} to resume the "Builder" pattern
     */
    public ContainerSyncBuilder sync()
    {
        setParentData();
        return new ContainerSyncBuilder(parent);
    }

    public BuiltContainer create(int windowId)
    {
        setParentData();
        return parent.create(windowId);
    }

    private void setParentData()
    {
        if (inventory.getSlots() != 0)
            parent.tileInventoryRanges.add(Range.between(rangeStart, parent.slots.size() - 1));
        parent.inventories.add(inventory);
    }
}

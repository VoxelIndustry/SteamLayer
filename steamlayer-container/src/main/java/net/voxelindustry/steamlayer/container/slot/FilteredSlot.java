package net.voxelindustry.steamlayer.container.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class FilteredSlot extends ListenerSlot
{
    private Predicate<ItemStack> filter;

    public FilteredSlot(Inventory inventory, int index, int xPosition, int yPosition)
    {
        super(inventory, index, xPosition, yPosition);
    }

    public FilteredSlot setFilter(Predicate<ItemStack> filter)
    {
        this.filter = filter;
        return this;
    }

    @Override
    public boolean canInsert(ItemStack stack)
    {
        return filter.test(stack);
    }
}

package net.voxelindustry.steamlayer.container.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Predicate;

public class FilteredSlot extends ListenerSlot
{
    private Predicate<ItemStack> filter;

    public FilteredSlot(IItemHandler inventory, int index, int xPosition, int yPosition)
    {
        super(inventory, index, xPosition, yPosition);
    }

    public FilteredSlot setFilter(Predicate<ItemStack> filter)
    {
        this.filter = filter;
        return this;
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return this.filter.test(stack);
    }
}

package net.voxelindustry.steamlayer.container.slot;

import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class FilteredSlot extends ListenerSlot
{
    private Predicate<ItemStack> filter;

    public FilteredSlot(Container inventory, int index, int xPosition, int yPosition)
    {
        super(inventory, index, xPosition, yPosition);
    }

    public FilteredSlot setFilter(Predicate<ItemStack> filter)
    {
        this.filter = filter;
        return this;
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return filter.test(stack);
    }
}

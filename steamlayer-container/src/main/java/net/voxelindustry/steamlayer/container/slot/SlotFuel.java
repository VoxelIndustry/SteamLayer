package net.voxelindustry.steamlayer.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public class SlotFuel extends FilteredSlot
{
    public SlotFuel(Container inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
        setFilter(stack -> AbstractFurnaceBlockEntity.isFuel(stack) || SlotFuel.isBucket(stack));
    }

    @Override
    public int getMaxStackSize(ItemStack stack)
    {
        return SlotFuel.isBucket(stack) ? 1 : super.getMaxStackSize(stack);
    }

    public static boolean isBucket(ItemStack stack)
    {
        return stack.getItem() == Items.BUCKET;
    }
}

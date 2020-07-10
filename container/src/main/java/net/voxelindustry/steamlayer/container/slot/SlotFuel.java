package net.voxelindustry.steamlayer.container.slot;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class SlotFuel extends FilteredSlot
{
    public SlotFuel(Inventory inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
        setFilter(stack -> AbstractFurnaceBlockEntity.canUseAsFuel(stack) || SlotFuel.isBucket(stack));
    }

    @Override
    public int getMaxStackAmount(ItemStack stack)
    {
        return SlotFuel.isBucket(stack) ? 1 : super.getMaxStackAmount(stack);
    }

    public static boolean isBucket(ItemStack stack)
    {
        return stack.getItem() == Items.BUCKET;
    }
}

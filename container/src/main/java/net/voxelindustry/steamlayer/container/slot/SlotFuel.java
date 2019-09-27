package net.voxelindustry.steamlayer.container.slot;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraftforge.items.IItemHandler;

public class SlotFuel extends FilteredSlot
{
    public SlotFuel(IItemHandler inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
        this.setFilter(stack -> FurnaceTileEntity.isFuel(stack) || SlotFuel.isBucket(stack));
    }

    @Override
    public int getItemStackLimit(ItemStack stack)
    {
        return SlotFuel.isBucket(stack) ? 1 : super.getItemStackLimit(stack);
    }

    public static boolean isBucket(ItemStack stack)
    {
        return stack.getItem() == Items.BUCKET;
    }
}

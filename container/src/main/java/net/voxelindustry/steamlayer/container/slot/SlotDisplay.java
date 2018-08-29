package net.voxelindustry.steamlayer.container.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SlotDisplay extends ListenerSlot
{
    public SlotDisplay(IItemHandler inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return false;
    }

    @Override
    public boolean getHasStack()
    {
        return false;
    }

    @Override
    public ItemStack decrStackSize(int par1)
    {
        return ItemStack.EMPTY;
    }
}

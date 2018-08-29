package net.voxelindustry.steamlayer.container.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class SlotOutput extends ListenerSlot
{
    public SlotOutput(IItemHandler inventory, int index, int xPosition, int yPosition)
    {
        super(inventory, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return false;
    }
}

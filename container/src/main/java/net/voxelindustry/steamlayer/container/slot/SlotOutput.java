package net.voxelindustry.steamlayer.container.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class SlotOutput extends ListenerSlot
{
    public SlotOutput(Inventory inventory, int index, int xPosition, int yPosition)
    {
        super(inventory, index, xPosition, yPosition);
    }

    @Override
    public boolean canInsert(ItemStack stack)
    {
        return false;
    }
}

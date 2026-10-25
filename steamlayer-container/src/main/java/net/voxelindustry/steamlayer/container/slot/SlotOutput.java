package net.voxelindustry.steamlayer.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class SlotOutput extends ListenerSlot
{
    public SlotOutput(Container inventory, int index, int xPosition, int yPosition)
    {
        super(inventory, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return false;
    }
}

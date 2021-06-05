package net.voxelindustry.steamlayer.container.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class SlotDisplay extends ListenerSlot
{
    public SlotDisplay(Inventory inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack)
    {
        return false;
    }

    @Override
    public boolean hasStack()
    {
        return false;
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack)
    {
    }

    @Override
    public ItemStack takeStack(int amount)
    {
        return ItemStack.EMPTY;
    }
}

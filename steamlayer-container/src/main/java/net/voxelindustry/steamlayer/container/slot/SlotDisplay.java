package net.voxelindustry.steamlayer.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SlotDisplay extends ListenerSlot
{
    public SlotDisplay(Container inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return false;
    }

    @Override
    public boolean hasItem()
    {
        return false;
    }

    @Override
    public void onTake(Player player, ItemStack stack)
    {
    }

    @Override
    public ItemStack remove(int amount)
    {
        return ItemStack.EMPTY;
    }
}

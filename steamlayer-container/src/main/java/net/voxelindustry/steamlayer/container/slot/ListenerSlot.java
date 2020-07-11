package net.voxelindustry.steamlayer.container.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.voxelindustry.steamlayer.inventory.InventoryHandler;

import java.util.function.Consumer;

public class ListenerSlot extends Slot
{
    private Consumer<ItemStack> onChange;

    public ListenerSlot(Inventory inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
    }

    public void setOnChange(Consumer<ItemStack> onChange)
    {
        this.onChange = onChange;
    }

    @Override
    public void markDirty()
    {
        super.markDirty();

        if (inventory instanceof InventoryHandler)
            ((InventoryHandler) inventory).notifySlotChange();

        if (onChange != null)
            onChange.accept(getStack());
    }
}

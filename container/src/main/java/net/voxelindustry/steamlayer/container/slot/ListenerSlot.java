package net.voxelindustry.steamlayer.container.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.voxelindustry.steamlayer.inventory.InventoryHandler;

import java.util.function.Consumer;

public class ListenerSlot extends SlotItemHandler
{
    private Consumer<ItemStack> onChange;

    public ListenerSlot(IItemHandler inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
    }

    public void setOnChange(Consumer<ItemStack> onChange)
    {
        this.onChange = onChange;
    }

    @Override
    public void onSlotChanged()
    {
        super.onSlotChanged();

        if (this.getItemHandler() instanceof InventoryHandler)
            ((InventoryHandler) this.getItemHandler()).notifySlotChange();

        if (this.onChange != null)
            this.onChange.accept(this.getStack());
    }
}

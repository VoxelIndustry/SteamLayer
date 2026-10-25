package net.voxelindustry.steamlayer.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.voxelindustry.steamlayer.inventory.InventoryHandler;

import java.util.function.Consumer;

public class ListenerSlot extends Slot
{
    private Consumer<ItemStack> onChange;

    public ListenerSlot(Container inventory, int index, int x, int y)
    {
        super(inventory, index, x, y);
    }

    public void setOnChange(Consumer<ItemStack> onChange)
    {
        this.onChange = onChange;
    }

    @Override
    public void setChanged()
    {
        super.setChanged();

        if (container instanceof InventoryHandler)
            ((InventoryHandler) container).notifySlotChange();

        if (onChange != null)
            onChange.accept(getItem());
    }
}

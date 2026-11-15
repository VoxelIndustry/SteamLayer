package net.voxelindustry.steamlayer.inventory;

import lombok.Getter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Getter
public class MappedInventoryHandler implements Container
{
    private final Container compose;
    private final int[]     slots;

    public MappedInventoryHandler(Container compose, int[] slots)
    {
        this.compose = compose;
        this.slots = slots;
    }

    @Override
    public void setItem(int slot, ItemStack stack)
    {
        validateSlotIndex(slot);

        compose.setItem(map(slot), stack);
    }

    @Override
    public void setChanged()
    {
        compose.setChanged();
    }

    @Override
    public boolean stillValid(Player player)
    {
        return compose.stillValid(player);
    }

    @Override
    public int getContainerSize()
    {
        return slots.length;
    }

    @Override
    public boolean isEmpty()
    {
        return compose.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot)
    {
        validateSlotIndex(slot);
        return compose.getItem(map(slot));
    }

    @Override
    public ItemStack removeItem(int slot, int amount)
    {
        validateSlotIndex(slot);
        return compose.removeItem(map(slot), amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot)
    {
        validateSlotIndex(slot);
        return compose.removeItemNoUpdate(map(slot));
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack)
    {
        validateSlotIndex(slot);
        return compose.canPlaceItem(map(slot), stack);
    }

    private int map(int slot)
    {
        return slots[slot];
    }

    private void validateSlotIndex(int slot)
    {
        if (slot < 0 || slot >= slots.length)
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + slots.length + ")");
    }

    @Override
    public void clearContent()
    {
        for (int slot : slots)
            compose.removeItemNoUpdate(slot);
    }
}

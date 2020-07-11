package net.voxelindustry.steamlayer.inventory;

import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

@Getter
public class MappedInventoryHandler implements Inventory
{
    private final Inventory compose;
    private final int[]     slots;

    public MappedInventoryHandler(Inventory compose, int[] slots)
    {
        this.compose = compose;
        this.slots = slots;
    }

    @Override
    public void setStack(int slot, ItemStack stack)
    {
        validateSlotIndex(slot);

        compose.setStack(map(slot), stack);
    }

    @Override
    public void markDirty()
    {
        compose.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player)
    {
        return compose.canPlayerUse(player);
    }

    @Override
    public int size()
    {
        return slots.length;
    }

    @Override
    public boolean isEmpty()
    {
        return compose.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot)
    {
        validateSlotIndex(slot);
        return compose.getStack(map(slot));
    }

    @Override
    public ItemStack removeStack(int slot, int amount)
    {
        validateSlotIndex(slot);
        return compose.removeStack(map(slot), amount);
    }

    @Override
    public ItemStack removeStack(int slot)
    {
        validateSlotIndex(slot);
        return compose.removeStack(map(slot));
    }

    @Override
    public boolean isValid(int slot, ItemStack stack)
    {
        validateSlotIndex(slot);
        return compose.isValid(map(slot), stack);
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
    public void clear()
    {
        for (int slot : slots)
            compose.removeStack(slot);
    }
}

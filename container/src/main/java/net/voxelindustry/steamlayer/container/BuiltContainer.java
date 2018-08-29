package net.voxelindustry.steamlayer.container;

import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;
import net.voxelindustry.steamlayer.common.container.ISyncableContainer;
import net.voxelindustry.steamlayer.container.sync.SyncableProperty;
import net.voxelindustry.steamlayer.inventory.InventoryHandler;
import net.voxelindustry.steamlayer.network.packet.ContainerUpdatePacket;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;
import net.voxelindustry.steamlayer.utils.ItemUtils;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BuiltContainer extends Container implements ISyncableContainer
{
    @Getter
    private final String name;

    private final EntityPlayer player;

    private final Predicate<EntityPlayer> canInteract;
    private final List<Range<Integer>>    playerSlotRanges;
    private final List<Range<Integer>>    tileSlotRanges;

    private List<SyncableProperty<?>>         syncablesValues;
    private List<Consumer<InventoryCrafting>> craftEvents;

    private final List<ItemStackHandler> inventories;

    BuiltContainer(String name, EntityPlayer player, List<ItemStackHandler> inventories,
                   Predicate<EntityPlayer> canInteract, List<Range<Integer>> playerSlotRange,
                   List<Range<Integer>> tileSlotRange)
    {
        this.player = player;
        this.name = name;

        this.canInteract = canInteract;

        this.playerSlotRanges = playerSlotRange;
        this.tileSlotRanges = tileSlotRange;

        this.inventories = inventories;

        this.inventories.forEach(inventory ->
        {
            if (inventory instanceof InventoryHandler)
                ((InventoryHandler) inventory).openInventory(player);
        });
    }

    public void setSyncables(List<SyncableProperty<?>> properties)
    {
        this.syncablesValues = properties;
    }

    public void addSyncable(SyncableProperty<?> property)
    {
        this.syncablesValues.add(property);
    }

    public void addCraftEvents(List<Consumer<InventoryCrafting>> craftEvents)
    {
        this.craftEvents = craftEvents;
    }

    public void addCraftEvent(Consumer<InventoryCrafting> craftEvent)
    {
        if (this.craftEvents == null)
            this.craftEvents = new ArrayList<>();
        this.craftEvents.add(craftEvent);
    }

    public void removeCraftEvent(Consumer<InventoryCrafting> craftEvent)
    {
        if (this.craftEvents == null)
            this.craftEvents = new ArrayList<>();
        this.craftEvents.remove(craftEvent);
    }

    public void addSlot(Slot slot)
    {
        this.addSlotToContainer(slot);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.canInteract.test(playerIn);
    }

    @Override
    public final void onCraftMatrixChanged(IInventory inv)
    {
        if (this.craftEvents != null && !this.craftEvents.isEmpty())
            this.craftEvents.forEach(consumer -> consumer.accept((InventoryCrafting) inv));
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if (this.syncablesValues != null && !this.syncablesValues.isEmpty())
        {
            for (SyncableProperty<?> syncable : this.syncablesValues)
            {
                if (syncable.needRefresh())
                {
                    syncable.updateInternal();
                    SteamLayerPacketHandler.INSTANCE.sendTo(new ContainerUpdatePacket(this.windowId,
                            this.syncablesValues.indexOf(syncable), syncable.toNBT(new NBTTagCompound())),
                            (EntityPlayerMP) this.player);
                }
            }
        }
    }

    @Override
    public void updateProperty(int id, NBTTagCompound property)
    {
        SyncableProperty<?> syncable = this.syncablesValues.get(id);
        syncable.fromNBT(property);
        syncable.update();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {

        ItemStack originalStack = ItemStack.EMPTY;

        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {

            ItemStack stackInSlot = slot.getStack();
            originalStack = stackInSlot.copy();

            boolean shifted = false;

            for (Range<Integer> range : this.playerSlotRanges)
                if (range.contains(index))
                {

                    if (this.shiftToTile(stackInSlot))
                        shifted = true;
                    break;
                }

            if (!shifted)
                for (Range<Integer> range : this.tileSlotRanges)
                    if (range.contains(index))
                    {
                        if (this.shiftToPlayer(stackInSlot))
                            shifted = true;
                        break;
                    }

            slot.onSlotChange(stackInSlot, originalStack);
            if (stackInSlot.getCount() <= 0)
                slot.putStack(ItemStack.EMPTY);
            slot.onSlotChanged();
            if (stackInSlot.getCount() == originalStack.getCount())
                return ItemStack.EMPTY;
            slot.onTake(player, stackInSlot);
        }
        return originalStack;
    }

    protected boolean shiftItemStack(ItemStack stackToShift, int start, int end)
    {
        boolean changed = false;
        if (stackToShift.isStackable())
        {
            for (int slotIndex = start; stackToShift.getCount() > 0 && slotIndex < end; slotIndex++)
            {
                Slot slot = this.inventorySlots.get(slotIndex);
                ItemStack stackInSlot = slot.getStack();
                if (!stackInSlot.isEmpty() && ItemUtils.deepEquals(stackInSlot, stackToShift)
                        && slot.isItemValid(stackToShift))
                {
                    int resultingStackSize = stackInSlot.getCount() + stackToShift.getCount();
                    int max = Math.min(stackToShift.getMaxStackSize(), slot.getSlotStackLimit());
                    if (resultingStackSize <= max)
                    {
                        stackToShift.setCount(0);
                        stackInSlot.setCount(resultingStackSize);
                        slot.onSlotChanged();
                        changed = true;
                    }
                    else if (stackInSlot.getCount() < max)
                    {
                        stackToShift.shrink(max - stackInSlot.getCount());
                        stackInSlot.setCount(max);
                        slot.onSlotChanged();
                        changed = true;
                    }
                }
            }
        }
        if (stackToShift.getCount() > 0)
        {
            for (int slotIndex = start; stackToShift.getCount() > 0 && slotIndex < end; slotIndex++)
            {
                Slot slot = this.inventorySlots.get(slotIndex);
                ItemStack stackInSlot = slot.getStack();
                if (stackInSlot.isEmpty() && slot.isItemValid(stackToShift))
                {
                    int max = Math.min(stackToShift.getMaxStackSize(), slot.getSlotStackLimit());
                    stackInSlot = stackToShift.copy();
                    stackInSlot.setCount(Math.min(stackToShift.getCount(), max));
                    stackToShift.shrink(stackInSlot.getCount());
                    slot.putStack(stackInSlot);
                    slot.onSlotChanged();
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean shiftToTile(ItemStack stackToShift)
    {
        for (Range<Integer> range : this.tileSlotRanges)
            if (this.shiftItemStack(stackToShift, range.getMinimum(), range.getMaximum() + 1))
                return true;
        return false;
    }

    private boolean shiftToPlayer(ItemStack stackToShift)
    {
        for (Range<Integer> range : this.playerSlotRanges)
            if (this.shiftItemStack(stackToShift, range.getMinimum(), range.getMaximum() + 1))
                return true;
        return false;
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);
        this.inventories.forEach(inventory ->
        {
            if (inventory instanceof InventoryHandler)
                ((InventoryHandler) inventory).closeInventory(player);
        });
    }
}
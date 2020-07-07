package net.voxelindustry.steamlayer.container;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.voxelindustry.steamlayer.common.container.ISyncedContainer;
import net.voxelindustry.steamlayer.common.utils.ItemUtils;
import net.voxelindustry.steamlayer.container.sync.ContainerSyncPacket;
import net.voxelindustry.steamlayer.container.sync.ISyncCallback;
import net.voxelindustry.steamlayer.container.sync.SyncedProperty;
import net.voxelindustry.steamlayer.container.sync.SyncedValue;
import net.voxelindustry.steamlayer.inventory.InventoryHandler;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BuiltContainer extends Container implements ISyncedContainer
{
    private final PlayerEntity player;

    private final Predicate<PlayerEntity> canInteract;
    private final List<Range<Integer>>    playerSlotRanges;
    private final List<Range<Integer>>    tileSlotRanges;

    private List<SyncedValue>               syncablesValues;
    private Map<String, SyncedValue>        namedSyncables;
    private Map<SyncedValue, ISyncCallback> syncCallbacks;

    private List<Consumer<CraftingInventory>> craftEvents;

    private final List<ItemStackHandler> inventories;

    @Setter
    private ContainerEvent       closeEvent;
    @Setter
    private List<ContainerEvent> tickEvents;

    private List<Slot> cachedInventorySlots;

    @Getter
    @Setter
    private TileEntity mainTile;

    BuiltContainer(ContainerType<BuiltContainer> type,
                   int windowId,
                   PlayerEntity player,
                   List<ItemStackHandler> inventories,
                   Predicate<PlayerEntity> canInteract,
                   List<Range<Integer>> playerSlotRange,
                   List<Range<Integer>> tileSlotRange)
    {
        super(type, windowId);

        this.player = player;

        this.canInteract = canInteract;

        playerSlotRanges = playerSlotRange;
        tileSlotRanges = tileSlotRange;

        this.inventories = inventories;

        this.inventories.forEach(inventory ->
        {
            if (inventory instanceof InventoryHandler)
                ((InventoryHandler) inventory).openInventory(player);
        });
    }

    public void setSyncables(List<SyncedValue> properties, Map<String, SyncedValue> namedSyncables)
    {
        syncablesValues = properties;
        this.namedSyncables = namedSyncables;
    }

    public void addSyncable(SyncedProperty<?> property)
    {
        syncablesValues.add(property);
    }

    public void addCraftEvents(List<Consumer<CraftingInventory>> craftEvents)
    {
        this.craftEvents = craftEvents;
    }

    public void addCraftEvent(Consumer<CraftingInventory> craftEvent)
    {
        if (craftEvents == null)
            craftEvents = new ArrayList<>();
        craftEvents.add(craftEvent);
    }

    public void removeCraftEvent(Consumer<CraftingInventory> craftEvent)
    {
        if (craftEvents == null)
            craftEvents = new ArrayList<>();
        craftEvents.remove(craftEvent);
    }

    public void addSyncCallback(String name, ISyncCallback callback)
    {
        if (!namedSyncables.containsKey(name))
            throw new RuntimeException("Cannot add callback to unknown SyncedProperty [" + name + "]");

        if (syncCallbacks == null)
            syncCallbacks = new HashMap<>();
        syncCallbacks.put(namedSyncables.get(name), callback);
    }

    @Override
    public Slot addSlot(Slot slot)
    {
        return super.addSlot(slot);
    }

    @Override
    public boolean canInteractWith(PlayerEntity player)
    {
        return canInteract.test(player);
    }

    @Override
    public final void onCraftMatrixChanged(IInventory inv)
    {
        if (craftEvents != null && !craftEvents.isEmpty())
            craftEvents.forEach(consumer -> consumer.accept((CraftingInventory) inv));
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        if (!tickEvents.isEmpty())
            tickEvents.forEach(event -> event.apply(this, player, false));

        if (syncablesValues == null || syncablesValues.isEmpty())
            return;

        for (SyncedValue synced : syncablesValues)
        {
            if (synced.needRefresh())
            {
                synced.updateInternal();
                new ContainerSyncPacket(windowId, syncablesValues.indexOf(synced), synced).sendTo((ServerPlayerEntity) player);
            }
        }
    }

    @Override
    public void updateProperty(int id, ByteBuf buffer)
    {
        SyncedValue property = syncablesValues.get(id);
        property.read(buffer);
        property.update();

        if (syncCallbacks != null && syncCallbacks.containsKey(property))
            syncCallbacks.get(property).onSync(property);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index)
    {

        ItemStack originalStack = ItemStack.EMPTY;

        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {

            ItemStack stackInSlot = slot.getStack();
            originalStack = stackInSlot.copy();

            boolean shifted = false;

            for (Range<Integer> range : playerSlotRanges)
                if (range.contains(index))
                {

                    if (shiftToTile(stackInSlot))
                        shifted = true;
                    break;
                }

            if (!shifted)
                for (Range<Integer> range : tileSlotRanges)
                    if (range.contains(index))
                    {
                        if (shiftToPlayer(stackInSlot))
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
                Slot slot = inventorySlots.get(slotIndex);
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
                Slot slot = inventorySlots.get(slotIndex);
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
        for (Range<Integer> range : tileSlotRanges)
            if (shiftItemStack(stackToShift, range.getMinimum(), range.getMaximum() + 1))
                return true;
        return false;
    }

    private boolean shiftToPlayer(ItemStack stackToShift)
    {
        for (Range<Integer> range : playerSlotRanges)
            if (shiftItemStack(stackToShift, range.getMinimum(), range.getMaximum() + 1))
                return true;
        return false;
    }

    @Override
    public void onContainerClosed(PlayerEntity player)
    {
        super.onContainerClosed(player);

        if (closeEvent != null)
            closeEvent.apply(this, player, player.world.isRemote);

        inventories.forEach(inventory ->
        {
            if (inventory instanceof InventoryHandler)
                ((InventoryHandler) inventory).closeInventory(player);
        });
    }

    public void hideAllSlots()
    {
        cachedInventorySlots = Lists.newArrayList(inventorySlots);
        inventorySlots.clear();
    }

    public void showAllSlots()
    {
        inventorySlots.addAll(cachedInventorySlots);
        cachedInventorySlots.clear();
    }
}
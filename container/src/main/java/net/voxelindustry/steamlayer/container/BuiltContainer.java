package net.voxelindustry.steamlayer.container;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.voxelindustry.steamlayer.common.container.ISyncedContainer;
import net.voxelindustry.steamlayer.common.utils.ItemUtils;
import net.voxelindustry.steamlayer.container.sync.ContainerSyncPacket;
import net.voxelindustry.steamlayer.container.sync.ISyncCallback;
import net.voxelindustry.steamlayer.container.sync.SyncedProperty;
import net.voxelindustry.steamlayer.container.sync.SyncedValue;
import net.voxelindustry.steamlayer.inventory.InventoryHandler;
import org.apache.commons.lang3.Range;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class BuiltContainer extends ScreenHandler implements ISyncedContainer
{
    private final PlayerEntity player;

    private final Predicate<PlayerEntity> canUse;
    private final List<Range<Integer>>    playerSlotRanges;
    private final List<Range<Integer>>    tileSlotRanges;

    private List<SyncedValue>               syncablesValues;
    private Map<String, SyncedValue>        namedSyncables;
    private Map<SyncedValue, ISyncCallback> syncCallbacks;

    private final List<Inventory> inventories;

    @Setter
    private ContainerEvent       closeEvent;
    @Setter
    private List<ContainerEvent> tickEvents;

    private List<Slot> cachedslots;

    @Getter
    @Setter
    private BlockEntity mainTile;

    BuiltContainer(ScreenHandlerType<BuiltContainer> type,
                   int windowId,
                   PlayerEntity player,
                   List<Inventory> inventories,
                   Predicate<PlayerEntity> canUse,
                   List<Range<Integer>> playerSlotRange,
                   List<Range<Integer>> tileSlotRange)
    {
        super(type, windowId);

        this.player = player;

        this.canUse = canUse;

        playerSlotRanges = playerSlotRange;
        tileSlotRanges = tileSlotRange;

        this.inventories = inventories;

        this.inventories.forEach(inventory ->
        {
            if (inventory instanceof InventoryHandler)
                inventory.onOpen(player);
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
    public boolean canUse(PlayerEntity player)
    {
        return canUse.test(player);
    }

    @Override
    public void sendContentUpdates()
    {
        super.sendContentUpdates();

        if (!tickEvents.isEmpty())
            tickEvents.forEach(event -> event.apply(this, player, false));

        if (syncablesValues == null || syncablesValues.isEmpty())
            return;

        for (SyncedValue synced : syncablesValues)
        {
            if (synced.needRefresh())
            {
                synced.updateInternal();
                new ContainerSyncPacket(syncId, syncablesValues.indexOf(synced), synced).sendTo((ServerPlayerEntity) player);
            }
        }
    }

    @Override
    public void updateProperty(int id, ByteBuf buffer)
    {
        SyncedValue property = syncablesValues.get(id);
        property.read(new PacketByteBuf(buffer));
        property.update();

        if (syncCallbacks != null && syncCallbacks.containsKey(property))
            syncCallbacks.get(property).onSync(property);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index)
    {
        ItemStack originalStack = ItemStack.EMPTY;

        Slot slot = slots.get(index);

        if (slot != null && slot.hasStack())
        {
            ItemStack stackInSlot = slot.getStack();
            originalStack = stackInSlot.copy();

            boolean shifted = false;

            for (Range<Integer> range : playerSlotRanges)
            {
                if (range.contains(index))
                {

                    if (shiftToTile(stackInSlot))
                        shifted = true;
                    break;
                }
            }

            if (!shifted)
            {
                for (Range<Integer> range : tileSlotRanges)
                {
                    if (range.contains(index))
                    {
                        if (shiftToPlayer(stackInSlot))
                            shifted = true;
                        break;
                    }
                }
            }

            slot.onStackChanged(stackInSlot, originalStack);
            if (stackInSlot.getCount() <= 0)
                slot.setStack(ItemStack.EMPTY);
            slot.markDirty();
            if (stackInSlot.getCount() == originalStack.getCount())
                return ItemStack.EMPTY;
            slot.onTakeItem(player, stackInSlot);
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
                Slot slot = slots.get(slotIndex);
                ItemStack stackInSlot = slot.getStack();
                if (!stackInSlot.isEmpty() && ItemUtils.deepEquals(stackInSlot, stackToShift)
                        && slot.canInsert(stackToShift))
                {
                    int resultingStackSize = stackInSlot.getCount() + stackToShift.getCount();
                    int max = Math.min(stackToShift.getMaxCount(), slot.getMaxStackAmount());
                    if (resultingStackSize <= max)
                    {
                        stackToShift.setCount(0);
                        stackInSlot.setCount(resultingStackSize);
                        slot.markDirty();
                        changed = true;
                    }
                    else if (stackInSlot.getCount() < max)
                    {
                        stackToShift.decrement(max - stackInSlot.getCount());
                        stackInSlot.setCount(max);
                        slot.markDirty();
                        changed = true;
                    }
                }
            }
        }
        if (stackToShift.getCount() > 0)
        {
            for (int slotIndex = start; stackToShift.getCount() > 0 && slotIndex < end; slotIndex++)
            {
                Slot slot = slots.get(slotIndex);
                ItemStack stackInSlot = slot.getStack();
                if (stackInSlot.isEmpty() && slot.canInsert(stackToShift))
                {
                    int max = Math.min(stackToShift.getMaxCount(), slot.getMaxStackAmount());
                    stackInSlot = stackToShift.copy();
                    stackInSlot.setCount(Math.min(stackToShift.getCount(), max));
                    stackToShift.decrement(stackInSlot.getCount());
                    slot.setStack(stackInSlot);
                    slot.markDirty();
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
    public void close(PlayerEntity player)
    {
        super.close(player);

        if (closeEvent != null)
            closeEvent.apply(this, player, player.world.isClient);

        inventories.forEach(inventory ->
        {
            if (inventory instanceof InventoryHandler)
                inventory.onClose(player);
        });
    }

    public void hideAllSlots()
    {
        cachedslots = Lists.newArrayList(slots);
        slots.clear();
    }

    public void showAllSlots()
    {
        slots.addAll(cachedslots);
        cachedslots.clear();
    }
}
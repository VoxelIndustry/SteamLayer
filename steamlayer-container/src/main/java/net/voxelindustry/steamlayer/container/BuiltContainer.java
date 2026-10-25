package net.voxelindustry.steamlayer.container;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.voxelindustry.steamlayer.common.container.ISyncedContainer;
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

public class BuiltContainer extends AbstractContainerMenu implements ISyncedContainer
{
    private final Player player;

    private final Predicate<Player> canUse;
    private final List<Range<Integer>>    playerSlotRanges;
    private final List<Range<Integer>>    tileSlotRanges;

    private List<SyncedValue>               syncablesValues;
    private Map<String, SyncedValue>        namedSyncables;
    private Map<SyncedValue, ISyncCallback> syncCallbacks;

    private final List<Container> inventories;

    @Setter
    private ContainerEvent       closeEvent;
    @Setter
    private List<ContainerEvent> tickEvents;

    private List<Slot> cachedslots;

    @Getter
    @Setter
    private BlockEntity mainTile;

    BuiltContainer(MenuType<BuiltContainer> type,
                   int windowId,
                   Player player,
                   List<Container> inventories,
                   Predicate<Player> canUse,
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
                inventory.startOpen(player);
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
    public boolean stillValid(Player player)
    {
        return canUse.test(player);
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();

        if (!tickEvents.isEmpty())
            tickEvents.forEach(event -> event.apply(this, player, false));

        if (syncablesValues == null || syncablesValues.isEmpty())
            return;

        for (SyncedValue synced : syncablesValues)
        {
            if (synced.needRefresh())
            {
                synced.updateInternal();
                new ContainerSyncPacket(containerId, syncablesValues.indexOf(synced), synced).sendTo((ServerPlayer) player);
            }
        }
    }

    @Override
    public void updateProperty(int id, ByteBuf buffer)
    {
        SyncedValue property = syncablesValues.get(id);
        property.read(new FriendlyByteBuf(buffer));
        property.update();

        if (syncCallbacks != null && syncCallbacks.containsKey(property))
            syncCallbacks.get(property).onSync(property);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack originalStack = ItemStack.EMPTY;

        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem())
        {
            ItemStack stackInSlot = slot.getItem();
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

            slot.onQuickCraft(stackInSlot, originalStack);
            if (stackInSlot.getCount() <= 0)
                slot.setByPlayer(ItemStack.EMPTY);
            slot.setChanged();
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
                Slot slot = slots.get(slotIndex);
                ItemStack stackInSlot = slot.getItem();
                if (!stackInSlot.isEmpty() && ItemStack.isSameItemSameTags(stackInSlot, stackToShift)
                        && slot.mayPlace(stackToShift))
                {
                    int resultingStackSize = stackInSlot.getCount() + stackToShift.getCount();
                    int max = Math.min(stackToShift.getMaxStackSize(), slot.getMaxStackSize(stackToShift));
                    if (resultingStackSize <= max)
                    {
                        stackToShift.setCount(0);
                        stackInSlot.setCount(resultingStackSize);
                        slot.setChanged();
                        changed = true;
                    }
                    else if (stackInSlot.getCount() < max)
                    {
                        stackToShift.shrink(max - stackInSlot.getCount());
                        stackInSlot.setCount(max);
                        slot.setChanged();
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
                ItemStack stackInSlot = slot.getItem();
                if (stackInSlot.isEmpty() && slot.mayPlace(stackToShift))
                {
                    int max = Math.min(stackToShift.getMaxStackSize(), slot.getMaxStackSize(stackToShift));
                    stackInSlot = stackToShift.copy();
                    stackInSlot.setCount(Math.min(stackToShift.getCount(), max));
                    stackToShift.shrink(stackInSlot.getCount());
                    slot.setByPlayer(stackInSlot);
                    slot.setChanged();
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
    public void removed(Player player)
    {
        super.removed(player);

        if (closeEvent != null)
            closeEvent.apply(this, player, player.level().isClientSide);

        inventories.forEach(inventory ->
        {
            if (inventory instanceof InventoryHandler)
                inventory.stopOpen(player);
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
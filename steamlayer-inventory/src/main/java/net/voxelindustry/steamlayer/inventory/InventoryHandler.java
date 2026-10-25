package net.voxelindustry.steamlayer.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.voxelindustry.steamlayer.common.utils.TagSerializable;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Getter
@Accessors(fluent = true)
public class InventoryHandler implements Container, TagSerializable<CompoundTag>
{
    @Setter
    private IntConsumer onSlotChange;

    private final Int2ObjectMap<Predicate<ItemStack>> slotFilters;

    @Setter
    private Consumer<Player> onOpen;
    @Setter
    private Consumer<Player> onClose;

    @Setter
    private Supplier<BlockPos> posSupplier;

    @Getter
    @Setter
    private boolean canDropContents = true;

    private final NonNullList<ItemStack> stacks;

    public InventoryHandler()
    {
        this(0);
    }

    public InventoryHandler(int size)
    {
        this(NonNullList.withSize(size, ItemStack.EMPTY));
    }

    public InventoryHandler(NonNullList<ItemStack> stacks)
    {
        this.stacks = stacks;

        slotFilters = new Int2ObjectOpenHashMap<>();
    }

    @Override
    public int getContainerSize()
    {
        return stacks.size();
    }

    @Override
    public boolean isEmpty()
    {
        for (ItemStack stack : stacks)
            if (!stack.isEmpty())
                return false;
        return true;
    }

    @Override
    public ItemStack getItem(int slot)
    {
        return stacks.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount)
    {
        ItemStack stack = ContainerHelper.removeItem(stacks, slot, amount);
        if (!stack.isEmpty())
            onContentsChanged(slot);

        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot)
    {
        ItemStack removed = removeItem(slot, getItem(slot).getCount());
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack)
    {
        stacks.set(slot, stack);

        onContentsChanged(slot);
    }

    @Override
    public int getMaxStackSize()
    {
        // FIXME : When an api allowing insert/extract is made. Fall back to the slotLimits map
        return 64;
    }

    @Override
    public void setChanged()
    {
    }

    @Override
    public boolean stillValid(Player player)
    {
        if (posSupplier == null)
            return true;

        BlockPos pos = posSupplier.get();
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }

    @Override
    public void startOpen(Player player)
    {
        if (onOpen != null)
            onOpen.accept(player);
    }

    @Override
    public void stopOpen(Player player)
    {
        if (onClose != null)
            onClose.accept(player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack)
    {
        if (!slotFilters.isEmpty() && slotFilters.containsKey(slot))
            return slotFilters.get(slot).test(stack);
        return true;
    }

    @Override
    public void clearContent()
    {
        stacks.forEach(stack -> stack.setCount(0));
        notifySlotChange();
    }

    public void addSlotFilter(int slot, Predicate<ItemStack> filter)
    {
        slotFilters.put(slot, filter);
    }

    public void addSlotFilters(Predicate<ItemStack> filter, int... slots)
    {
        for (int slot : slots)
            addSlotFilter(slot, filter);
    }

    public void notifySlotChange()
    {
        if (onSlotChange != null)
            onSlotChange.accept(-1);
    }

    protected void onContentsChanged(int slot)
    {
        if (onSlotChange != null)
            onSlotChange.accept(slot);

        setChanged();
    }

    @Override
    public CompoundTag toTag()
    {
        return ContainerHelper.saveAllItems(new CompoundTag(), stacks);
    }

    @Override
    public void fromTag(CompoundTag tag)
    {
        ContainerHelper.loadAllItems(tag, stacks);
    }
}

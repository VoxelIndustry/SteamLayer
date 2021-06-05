package net.voxelindustry.steamlayer.recipe.ingredient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.voxelindustry.steamlayer.common.utils.ItemUtils;

@RequiredArgsConstructor
public class ItemStackIngredientHandler implements IngredientHandler<ItemStack>
{
    @Getter
    private final Identifier identifier;

    @Override
    public ItemStack accumulateMutator(ItemStack first, ItemStack second)
    {
        first.increment(second.getCount());
        return first;
    }

    @Override
    public ItemStack accumulateCreate(ItemStack first, ItemStack second)
    {
        return ItemUtils.copyWithSize(first, first.getCount() + second.getCount());
    }

    @Override
    public ItemStack subtractMutator(ItemStack first, ItemStack second)
    {
        first.decrement(second.getCount());
        return first;
    }

    @Override
    public ItemStack subtractCreate(ItemStack first, ItemStack second)
    {
        return ItemUtils.copyWithSize(first, first.getCount() - second.getCount());
    }

    @Override
    public boolean canMerge(ItemStack first, ItemStack second)
    {
        return ItemUtils.canMergeStacks(first, second);
    }

    @Override
    public boolean isEmpty(ItemStack value)
    {
        return value.isEmpty();
    }

    @Override
    public ItemStack getEmpty()
    {
        return ItemStack.EMPTY;
    }

    @Override
    public NbtCompound toTag(ItemStack value, NbtCompound tag)
    {
        return value.writeNbt(tag);
    }

    @Override
    public ItemStack fromTag(NbtCompound tag)
    {
        return ItemStack.fromNbt(tag);
    }
}

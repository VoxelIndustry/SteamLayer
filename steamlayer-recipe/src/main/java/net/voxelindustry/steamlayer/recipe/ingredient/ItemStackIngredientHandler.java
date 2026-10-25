package net.voxelindustry.steamlayer.recipe.ingredient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.voxelindustry.steamlayer.common.utils.ItemUtils;

@RequiredArgsConstructor
public class ItemStackIngredientHandler implements IngredientHandler<ItemStack>
{
    @Getter
    private final ResourceLocation identifier;

    @Override
    public ItemStack accumulateMutator(ItemStack first, ItemStack second)
    {
        first.grow(second.getCount());
        return first;
    }

    @Override
    public ItemStack accumulateCreate(ItemStack first, ItemStack second)
    {
        return first.copyWithCount(first.getCount() + second.getCount());
    }

    @Override
    public ItemStack subtractMutator(ItemStack first, ItemStack second)
    {
        first.shrink(second.getCount());
        return first;
    }

    @Override
    public ItemStack subtractCreate(ItemStack first, ItemStack second)
    {
        return first.copyWithCount(first.getCount() - second.getCount());
    }

    @Override
    public boolean canMerge(ItemStack first, ItemStack second)
    {
        return ItemUtils.canMerge(first, second);
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
    public CompoundTag toTag(ItemStack value, CompoundTag tag)
    {
        return value.save(tag);
    }

    @Override
    public ItemStack fromTag(CompoundTag tag)
    {
        return ItemStack.of(tag);
    }
}

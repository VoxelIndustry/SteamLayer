package net.voxelindustry.steamlayer.recipe.ingredient;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Range;

public class ItemStackRandRecipeIngredient extends ItemStackRecipeIngredient
{
    private Range<Integer> range;

    public ItemStackRandRecipeIngredient(ItemStack ingredient, Range<Integer> range)
    {
        super(ingredient);

        this.range = range;
    }

    @Override
    public ItemStack getRaw()
    {
        ItemStack copy = ingredient.isEmpty() ? cachedStack.copy() : ingredient.copy();

        copy.setCount(RandomUtils.nextInt(range.getMinimum(), range.getMaximum()));
        return copy;
    }
}

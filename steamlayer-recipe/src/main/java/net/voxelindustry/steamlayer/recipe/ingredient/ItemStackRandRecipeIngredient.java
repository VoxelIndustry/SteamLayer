package net.voxelindustry.steamlayer.recipe.ingredient;

import net.minecraft.item.ItemStack;
import net.voxelindustry.steamlayer.common.utils.ItemUtils;
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
        return ItemUtils.copyWithSize(ingredient, RandomUtils.nextInt(range.getMinimum(), range.getMaximum()));
    }

    @Override
    public RecipeIngredient<ItemStack> staticForState()
    {
        return new ItemStackRecipeIngredient(getRaw());
    }
}

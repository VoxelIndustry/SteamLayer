package net.voxelindustry.steamlayer.recipe.ingredient;

import net.minecraft.item.ItemStack;
import net.voxelindustry.steamlayer.common.utils.ItemUtils;

public class ItemStackRecipeIngredient extends RecipeIngredient<ItemStack>
{
    protected final int       quantity;
    protected final ItemStack cachedStack;
    protected final ItemStack ingredient;

    public ItemStackRecipeIngredient(ItemStack ingredient)
    {
        this.ingredient = ingredient;
        quantity = 0;

        cachedStack = ItemStack.EMPTY;
    }

    @Override
    public boolean match(ItemStack against)
    {
        return ItemUtils.deepEquals(ingredient, against);
    }

    @Override
    public boolean matchWithQuantity(ItemStack against)
    {
        if (!ingredient.isEmpty())
            return ingredient.getCount() <= against.getCount() && ItemUtils.deepEquals(ingredient, against);
        else
            return quantity <= against.getCount();
    }

    @Override
    public int getQuantity()
    {
        if (!ingredient.isEmpty())
            return ingredient.getCount();
        return quantity;
    }

    @Override
    public ItemStack getRaw()
    {
        if (!ingredient.isEmpty())
            return ingredient;
        return cachedStack;
    }
}

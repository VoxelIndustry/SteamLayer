package net.voxelindustry.steamlayer.recipe.ingredient;

import net.minecraft.item.ItemStack;
import net.voxelindustry.steamlayer.common.utils.ItemUtils;

public class ItemStackRecipeIngredient extends RecipeIngredient<ItemStack>
{
    protected final int       quantity;
    protected final ItemStack ingredient;

    protected final boolean strictTagComparing;

    public ItemStackRecipeIngredient(ItemStack ingredient, boolean strictTagComparing)
    {
        this.ingredient = ingredient;
        quantity = 0;

        this.strictTagComparing = strictTagComparing;
    }

    public ItemStackRecipeIngredient(ItemStack ingredient)
    {
        this(ingredient, true);
    }

    @Override
    public boolean match(ItemStack against)
    {
        if (strictTagComparing)
            return ItemUtils.deepEquals(ingredient, against);
        return ItemUtils.equalsIgnoringTag(ingredient, against);
    }

    @Override
    public boolean matchWithQuantity(ItemStack against)
    {
        if (!ingredient.isEmpty())
            return ingredient.getCount() <= against.getCount() && match(against);
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
        return ingredient.copy();
    }
}

package net.voxelindustry.steamlayer.recipe.ingredient;

public abstract class RecipeIngredient<T>
{
    public abstract boolean match(T against);

    public abstract boolean matchWithQuantity(T against);

    public abstract int getQuantity();

    public abstract T getRaw();
}

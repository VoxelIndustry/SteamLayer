package net.voxelindustry.steamlayer.recipe.ingredient;

public abstract class RecipeIngredient<T>
{
    public abstract boolean match(T against);

    public abstract boolean matchWithQuantity(T against);

    public abstract int getQuantity();

    public abstract T getRaw();

    /**
     * Static instance of this RecipeIngredient used for RecipeState storage.
     * <p>
     * Most ingredients are already static and may leave the default return of themselves.
     * Dynamics ingredients such as randomized quantities must return a fixed version corresponding to the selected amount.
     *
     * @return the fixed instance of this RecipeIngredient
     */
    public RecipeIngredient<T> staticForState()
    {
        return this;
    }
}

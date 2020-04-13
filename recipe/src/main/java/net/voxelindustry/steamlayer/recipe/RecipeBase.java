package net.voxelindustry.steamlayer.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.voxelindustry.steamlayer.recipe.ingredient.RecipeIngredient;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public abstract class RecipeBase
{
    protected final Map<Class<?>, NonNullList<RecipeIngredient<?>>> inputs  = new IdentityHashMap<>();
    protected final Map<Class<?>, NonNullList<RecipeIngredient<?>>> outputs = new IdentityHashMap<>();

    public boolean hasInputType(Class<?> input)
    {
        return inputs.containsKey(input);
    }

    public boolean hasOutputType(Class<?> output)
    {
        return outputs.containsKey(output);
    }

    public <T> NonNullList<RecipeIngredient<T>> getRecipeInputs(Class<T> clazz)
    {
        if (hasInputType(clazz))
            return (NonNullList<RecipeIngredient<T>>) (Object) inputs.get(clazz);
        return NonNullList.create();
    }

    public <T> NonNullList<RecipeIngredient<T>> getRecipeOutputs(Class<T> clazz)
    {
        if (hasOutputType(clazz))
            return (NonNullList<RecipeIngredient<T>>) (Object) outputs.get(clazz);
        return NonNullList.create();
    }

    public Optional<NonNullList<ItemStack>> getRemainingItems()
    {
        return Optional.empty();
    }

    public abstract int getTime();
}

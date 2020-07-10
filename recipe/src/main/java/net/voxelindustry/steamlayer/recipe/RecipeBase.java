package net.voxelindustry.steamlayer.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.voxelindustry.steamlayer.recipe.ingredient.RecipeIngredient;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public abstract class RecipeBase
{
    protected final Map<Class<?>, DefaultedList<RecipeIngredient<?>>> inputs  = new IdentityHashMap<>();
    protected final Map<Class<?>, DefaultedList<RecipeIngredient<?>>> outputs = new IdentityHashMap<>();

    public boolean hasInputType(Class<?> input)
    {
        return inputs.containsKey(input);
    }

    public boolean hasOutputType(Class<?> output)
    {
        return outputs.containsKey(output);
    }

    public <T> DefaultedList<RecipeIngredient<T>> getRecipeInputs(Class<T> clazz)
    {
        if (hasInputType(clazz))
            return (DefaultedList<RecipeIngredient<T>>) (Object) inputs.get(clazz);
        return DefaultedList.of();
    }

    public <T> DefaultedList<RecipeIngredient<T>> getRecipeOutputs(Class<T> clazz)
    {
        if (hasOutputType(clazz))
            return (DefaultedList<RecipeIngredient<T>>) (Object) outputs.get(clazz);
        return DefaultedList.of();
    }

    public Optional<DefaultedList<ItemStack>> getRemainingItems()
    {
        return Optional.empty();
    }

    public abstract int getTime();
}

package net.voxelindustry.steamlayer.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.voxelindustry.steamlayer.recipe.ingredient.RecipeIngredient;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public abstract class RecipeBase
{
    protected final Map<Class<?>, DefaultedList<RecipeIngredient<?>>> inputs  = new IdentityHashMap<>();
    protected final Map<Class<?>, DefaultedList<RecipeIngredient<?>>> outputs = new IdentityHashMap<>();

    private int[] essentialInputsCache;

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

    /**
     * Used to retrieve inputs slots to be validated for recipe retrieval by the Category
     *
     * @return array of the slots
     * @see net.voxelindustry.steamlayer.recipe.category.RecipeCategory#findRecipes(Object...)
     */
    public int[] getEssentialInputSlots()
    {
        if (essentialInputsCache == null)
            essentialInputsCache = IntStream.range(0, inputs.size()).toArray();
        return essentialInputsCache;
    }

    public abstract int getTime();
}

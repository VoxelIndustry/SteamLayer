package net.voxelindustry.steamlayer.recipe;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.voxelindustry.steamlayer.recipe.ingredient.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class RecipeBase
{
    protected final Multimap<Class<?>, RecipeIngredient<?>> inputs  = Multimaps.newListMultimap(Maps.newIdentityHashMap(), ArrayList::new);
    protected final Multimap<Class<?>, RecipeIngredient<?>> outputs = Multimaps.newListMultimap(Maps.newIdentityHashMap(), ArrayList::new);

    public boolean hasInputType(Class<?> input)
    {
        return inputs.containsKey(input);
    }

    public boolean hasOutputType(Class<?> output)
    {
        return outputs.containsKey(output);
    }

    @SuppressWarnings("unchecked")
    public <T> List<RecipeIngredient<T>> getRecipeInputs(Class<T> clazz)
    {
        if (hasInputType(clazz))
            return (List<RecipeIngredient<T>>) (Object) inputs.get(clazz);
        return DefaultedList.of();
    }

    @SuppressWarnings("unchecked")
    public <T> List<RecipeIngredient<T>> getRecipeOutputs(Class<T> clazz)
    {
        if (hasOutputType(clazz))
            return (List<RecipeIngredient<T>>) (Object) outputs.get(clazz);
        return DefaultedList.of();
    }

    public Optional<List<ItemStack>> getRemainingItems()
    {
        return Optional.empty();
    }

    public abstract int getTime();

    @SafeVarargs
    protected final <T> void addInputs(Class<T> inputType, RecipeIngredient<T> firstInput, RecipeIngredient<T>... inputs)
    {
        this.inputs.put(inputType, firstInput);
        for (RecipeIngredient<T> input : inputs)
            this.inputs.put(inputType, input);
    }

    @SafeVarargs
    protected final <T> void addOutputs(Class<T> outputType, RecipeIngredient<T> firstOutput, RecipeIngredient<T>... outputs)
    {
        this.outputs.put(outputType, firstOutput);
        for (RecipeIngredient<T> output : outputs)
            this.outputs.put(outputType, output);
    }

    protected final <T> void addInput(Class<T> inputType, RecipeIngredient<T> input)
    {
        inputs.put(inputType, input);
    }

    protected final <T> void addOutput(Class<T> outputType, RecipeIngredient<T> output)
    {
        outputs.put(outputType, output);
    }
}

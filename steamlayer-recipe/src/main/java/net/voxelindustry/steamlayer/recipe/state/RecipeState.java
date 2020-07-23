package net.voxelindustry.steamlayer.recipe.state;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.voxelindustry.steamlayer.common.utils.TagSerializable;
import net.voxelindustry.steamlayer.recipe.RecipeBase;
import net.voxelindustry.steamlayer.recipe.RecipeHandler;
import net.voxelindustry.steamlayer.recipe.ingredient.IngredientHandler;
import net.voxelindustry.steamlayer.recipe.ingredient.RecipeIngredient;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class RecipeState implements TagSerializable<CompoundTag>
{
    protected Multimap<Class<?>, RecipeIngredient<?>> inputs;
    protected Multimap<Class<?>, RecipeIngredient<?>> outputs;

    protected final Multimap<Class<?>, Object> consumedInputs = Multimaps.newListMultimap(Maps.newIdentityHashMap(), ArrayList::new);
    private final   Multimap<Class<?>, Object> leftInputs;

    private int recipeTime;

    private int currentTime;

    private final RecipeBase recipe;

    public RecipeState(RecipeBase recipe, Multimap<Class<?>, RecipeIngredient<?>> inputs, Multimap<Class<?>, RecipeIngredient<?>> outputs, int recipeTime)
    {
        this.recipe = recipe;

        this.inputs = inputs;
        this.outputs = outputs;

        this.recipeTime = recipeTime;

        leftInputs = Multimaps.newListMultimap(Maps.newIdentityHashMap(), ArrayList::new);
        inputs.forEach((typeClass, ingredient) -> leftInputs.put(typeClass, ingredient.getRaw()));
    }

    public void tick(int amount)
    {
        currentTime += amount;
    }

    public boolean isCompleted()
    {
        boolean completed = true;

        for (Class<?> typeClass : leftInputs.keySet())
        {
            if (!isLeftInputTypeEmpty(typeClass))
            {
                completed = false;
                break;
            }
        }

        return currentTime >= recipeTime && completed;
    }

    public void complete(World world, BlockPos pos, LivingEntity crafter)
    {
        recipe.onCraft(world, pos, this, crafter);
    }

    @SuppressWarnings("unchecked")
    private <T> boolean isLeftInputTypeEmpty(Class<T> typeClass)
    {
        IngredientHandler<T> handler = RecipeHandler.getIngredientHandler(typeClass);

        return leftInputs.get(typeClass).stream().allMatch(value -> handler.isEmpty((T) value));
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Experimental
    // FIXME: Does not handle remainders
    public <T> void consumeSlotless(Class<T> typeClass, T input)
    {
        List<RecipeIngredient<?>> ingredientsForType = (List<RecipeIngredient<?>>) inputs.get(typeClass);

        IngredientHandler<T> ingredientHandler = RecipeHandler.getIngredientHandler(typeClass);
        List<T> leftInputsForType = (List<T>) leftInputs.get(typeClass);

        int index = 0;
        for (RecipeIngredient<?> candidate : ingredientsForType)
        {
            RecipeIngredient<T> candidateOfType = (RecipeIngredient<T>) candidate;
            if (candidateOfType.match(input))
            {
                if (!ingredientHandler.isEmpty(leftInputsForType.get(index)))
                {
                    consumeSlotted(typeClass, input, index);
                    break;
                }
            }
            index++;
        }
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Experimental
    public <T> void consumeSlotted(Class<T> typeClass, T ingredient, int slot)
    {
        if (inputs.get(typeClass).size() <= slot || slot < 0)
            throw new IllegalArgumentException("Slot must be positive and less than the corresponding inputs type list size. slot=" + slot + ", listSize=" + inputs.get(typeClass).size() + ", type=" + typeClass.getSimpleName() + ", ingredient=" + ingredient);

        Collection<Object> inputs = consumedInputs.get(typeClass);

        IngredientHandler<T> ingredientHandler = RecipeHandler.getIngredientHandler(typeClass);
        Optional<T> existingIngredient = (Optional<T>) inputs.stream().filter(object -> ingredientHandler.canMerge(ingredient, (T) object)).findFirst();

        if (existingIngredient.isPresent())
            ingredientHandler.accumulateMutator(existingIngredient.get(), ingredient);
        else
            consumedInputs.put(typeClass, ingredient);

        List<T> leftInputsForType = (List<T>) leftInputs.get(typeClass);
        leftInputsForType.set(slot, ingredientHandler.subtractMutator(leftInputsForType.get(slot), ingredient));
    }

    public <T> List<T> getIngredientsLeft(Class<T> typeClass)
    {
        return (List<T>) leftInputs.get(typeClass);
    }

    public <T> List<T> getIngredientsConsumed(Class<T> typeClass)
    {
        return (List<T>) consumedInputs.get(typeClass);
    }

    @SuppressWarnings("unchecked")
    public <T> List<RecipeIngredient<T>> getInputs(Class<T> typeClass)
    {
        return (List<RecipeIngredient<T>>) (Object) inputs.get(typeClass);
    }

    @SuppressWarnings("unchecked")
    public <T> List<RecipeIngredient<T>> getOutputs(Class<T> typeClass)
    {
        return (List<RecipeIngredient<T>>) (Object) outputs.get(typeClass);
    }

    @Override
    public CompoundTag toTag()
    {
        CompoundTag tag = new CompoundTag();

        tag.putInt("currentTime", currentTime);
        tag.putInt("recipeTime", recipeTime);

        tag.putInt("leftInputTypesCount", leftInputs.keySet().size());

        int index = 0;
        for (Class<?> typeClass : leftInputs.keySet())
        {
            String identifier = RecipeHandler.getIngredientHandlerIdentifier(typeClass).toString();
            tag.putString("leftInputType" + index, identifier);

            leftInputForTypeToTag(tag, typeClass, identifier);
            index++;
        }

        tag.putInt("consumedInputTypesCount", consumedInputs.keySet().size());
        index = 0;
        for (Class<?> typeClass : consumedInputs.keySet())
        {
            String identifier = RecipeHandler.getIngredientHandlerIdentifier(typeClass).toString();
            tag.putString("consumedInputType" + index, identifier);

            consumedInputForTypeToTag(tag, typeClass, identifier);
            index++;
        }

        return tag;
    }

    @SuppressWarnings("unchecked")
    private <T> void consumedInputForTypeToTag(CompoundTag tag, Class<T> typeClass, String identifier)
    {
        IngredientHandler<T> ingredientHandler = RecipeHandler.getIngredientHandler(typeClass);
        Collection<T> consumedInputsForType = (Collection<T>) consumedInputs.get(typeClass);

        tag.putInt("consumedInputForType" + identifier, consumedInputsForType.size());
        int index = 0;
        for (T input : consumedInputsForType)
        {
            tag.put("consumedInputForType" + identifier + index, ingredientHandler.toTag(input, new CompoundTag()));
            index++;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void leftInputForTypeToTag(CompoundTag tag, Class<T> typeClass, String identifier)
    {
        IngredientHandler<T> ingredientHandler = RecipeHandler.getIngredientHandler(typeClass);
        Collection<T> leftInputsForType = (Collection<T>) leftInputs.get(typeClass);

        tag.putInt("leftInputForType" + identifier, leftInputsForType.size());
        int index = 0;
        for (T input : leftInputsForType)
        {
            tag.put("leftInputForType" + identifier + index, ingredientHandler.toTag(input, new CompoundTag()));
            index++;
        }
    }

    @Override
    public void fromTag(CompoundTag tag)
    {
        consumedInputs.clear();
        leftInputs.clear();

        currentTime = tag.getInt("currentTime");
        recipeTime = tag.getInt("recipeTime");

        int leftInputTypesCount = tag.getInt("leftInputTypesCount");

        for (int index = 0; index < leftInputTypesCount; index++)
        {
            String identifier = tag.getString("leftInputType" + index);
            Class<?> handlerType = RecipeHandler.getIngredientHandlerClass(new Identifier(identifier));

            leftInputForTypeFromTag(tag, handlerType, identifier);
        }

        int consumedInputTypesCount = tag.getInt("consumedInputTypesCount");

        for (int index = 0; index < consumedInputTypesCount; index++)
        {
            String identifier = tag.getString("consumedInputType" + index);
            Class<?> handlerType = RecipeHandler.getIngredientHandlerClass(new Identifier(identifier));

            consumedInputForTypeFromTag(tag, handlerType, identifier);
        }
    }

    private <T> void leftInputForTypeFromTag(CompoundTag tag, Class<T> typeClass, String identifier)
    {
        int leftInputForType = tag.getInt("leftInputForType" + identifier);
        IngredientHandler<T> ingredientHandler = RecipeHandler.getIngredientHandler(typeClass);

        for (int index = 0; index < leftInputForType; index++)
            leftInputs.put(typeClass, ingredientHandler.fromTag(tag.getCompound("leftInputForType" + identifier + index)));
    }

    private <T> void consumedInputForTypeFromTag(CompoundTag tag, Class<T> typeClass, String identifier)
    {
        int consumedInputForType = tag.getInt("consumedInputForType" + identifier);
        IngredientHandler<T> ingredientHandler = RecipeHandler.getIngredientHandler(typeClass);

        for (int index = 0; index < consumedInputForType; index++)
            consumedInputs.put(typeClass, ingredientHandler.fromTag(tag.getCompound("consumedInputForType" + identifier + index)));
    }
}

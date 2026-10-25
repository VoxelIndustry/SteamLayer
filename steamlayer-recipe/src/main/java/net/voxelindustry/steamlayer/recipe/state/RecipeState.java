package net.voxelindustry.steamlayer.recipe.state;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.voxelindustry.steamlayer.common.utils.TagSerializable;
import net.voxelindustry.steamlayer.recipe.IngredientManager;
import net.voxelindustry.steamlayer.recipe.RecipeBase;
import net.voxelindustry.steamlayer.recipe.category.RecipeCategory;
import net.voxelindustry.steamlayer.recipe.ingredient.IngredientHandler;
import net.voxelindustry.steamlayer.recipe.ingredient.RecipeIngredient;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

public class RecipeState implements TagSerializable<CompoundTag>
{
    protected Multimap<Class<?>, RecipeIngredient<?>> inputs;
    protected Multimap<Class<?>, RecipeIngredient<?>> outputs;

    protected final Multimap<Class<?>, Object> consumedInputs = Multimaps.newListMultimap(Maps.newIdentityHashMap(), ArrayList::new);
    private final Multimap<Class<?>, Object> leftInputs = Multimaps.newListMultimap(Maps.newIdentityHashMap(), ArrayList::new);

    private int recipeTime;

    private int currentTime;

    @Getter
    private final RecipeBase recipe;

    public RecipeState(RecipeBase recipe, Multimap<Class<?>, RecipeIngredient<?>> inputs, Multimap<Class<?>, RecipeIngredient<?>> outputs, int recipeTime)
    {
        this.recipe = recipe;

        this.inputs = inputs;
        this.outputs = outputs;

        this.recipeTime = recipeTime;

        inputs.forEach((typeClass, ingredient) -> leftInputs.put(typeClass, ingredient.getRaw()));
    }

    public void tick(int amount)
    {
        currentTime += amount;
    }

    public int currentTime()
    {
        return currentTime;
    }

    public float completionDelta()
    {
        return currentTime / (float) recipeTime;
    }

    public boolean isCompleted()
    {
        for (var typeClass : leftInputs.keySet())
        {
            if (!isLeftInputTypeEmpty(typeClass))
            {
                return false;
            }
        }

        return currentTime >= recipeTime;
    }

    public void complete(Level world, BlockPos pos, LivingEntity crafter)
    {
        recipe.onCraft(world, pos, this, crafter);
    }

    public void reset()
    {
        leftInputs.clear();
        consumedInputs.clear();

        currentTime = 0;
        inputs.forEach((typeClass, ingredient) -> leftInputs.put(typeClass, ingredient.getRaw()));
    }

    @SuppressWarnings("unchecked")
    private <T> boolean isLeftInputTypeEmpty(Class<T> typeClass)
    {
        IngredientHandler<T> handler = IngredientManager.getIngredientHandler(typeClass);

        return leftInputs.get(typeClass).stream().allMatch(value -> handler.isEmpty((T) value));
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Experimental
    // FIXME: Does not handle remainders
    public <T> void consumeSlotless(Class<T> typeClass, T input)
    {
        var ingredientsForType = (List<RecipeIngredient<?>>) inputs.get(typeClass);

        var ingredientHandler = IngredientManager.getIngredientHandler(typeClass);
        var leftInputsForType = (List<T>) leftInputs.get(typeClass);

        int index = 0;
        for (var candidate : ingredientsForType)
        {
            var candidateOfType = (RecipeIngredient<T>) candidate;
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

        var inputs = consumedInputs.get(typeClass);

        var ingredientHandler = IngredientManager.getIngredientHandler(typeClass);
        var existingIngredient = (Optional<T>) inputs.stream().filter(object -> ingredientHandler.canMerge(ingredient, (T) object)).findFirst();

        if (existingIngredient.isPresent())
            ingredientHandler.accumulateMutator(existingIngredient.get(), ingredient);
        else
            consumedInputs.put(typeClass, ingredient);

        var leftInputsForType = (List<T>) leftInputs.get(typeClass);
        leftInputsForType.set(slot, ingredientHandler.subtractMutator(leftInputsForType.get(slot), ingredient));
    }

    public <T> List<T> getIngredientsLeft(Class<T> typeClass)
    {
        if (leftInputs.containsKey(typeClass))
            return (List<T>) leftInputs.get(typeClass);
        return emptyList();
    }

    public <T> T getIngredientLeft(Class<T> typeClass, int slot)
    {
        var ingredientsLeft = getIngredientsLeft(typeClass);

        if (ingredientsLeft.size() > slot)
            return ingredientsLeft.get(slot);
        return IngredientManager.getIngredientHandler(typeClass).getEmpty();
    }

    public <T> List<T> getIngredientsConsumed(Class<T> typeClass)
    {
        if (consumedInputs.containsKey(typeClass))
            return (List<T>) consumedInputs.get(typeClass);
        return emptyList();
    }

    public <T> T getIngredientConsumed(Class<T> typeClass, int slot)
    {
        var ingredientsConsumed = getIngredientsConsumed(typeClass);

        if (ingredientsConsumed.size() > slot)
            return ingredientsConsumed.get(slot);
        return IngredientManager.getIngredientHandler(typeClass).getEmpty();
    }

    @SuppressWarnings("unchecked")
    public <T> List<RecipeIngredient<T>> getInputs(Class<T> typeClass)
    {
        if (inputs.containsKey(typeClass))
            return (List<RecipeIngredient<T>>) (Object) inputs.get(typeClass);
        return emptyList();
    }

    public <T> T getInput(Class<T> typeClass, int slot)
    {
        var inputs = getInputs(typeClass);

        if (inputs.size() > slot)
            return inputs.get(slot).getRaw();
        return IngredientManager.getIngredientHandler(typeClass).getEmpty();
    }

    @SuppressWarnings("unchecked")
    public <T> List<RecipeIngredient<T>> getOutputs(Class<T> typeClass)
    {
        if (outputs.containsKey(typeClass))
            return (List<RecipeIngredient<T>>) (Object) outputs.get(typeClass);
        return emptyList();
    }

    public <T> T getOutput(Class<T> typeClass, int slot)
    {
        var outputs = getOutputs(typeClass);

        if (outputs.size() > slot)
            return outputs.get(slot).getRaw();
        return IngredientManager.getIngredientHandler(typeClass).getEmpty();
    }

    @Override
    public CompoundTag toTag()
    {
        var tag = new CompoundTag();

        tag.putString("recipeNamespace", recipe.getId().getNamespace());
        tag.putString("recipePath", recipe.getId().getPath());

        tag.putInt("currentTime", currentTime);
        tag.putInt("recipeTime", recipeTime);

        tag.putInt("leftInputTypesCount", leftInputs.keySet().size());

        int index = 0;
        for (var typeClass : leftInputs.keySet())
        {
            var identifier = IngredientManager.getIngredientHandlerIdentifier(typeClass).toString();
            tag.putString("leftInputType" + index, identifier);

            leftInputForTypeToTag(tag, typeClass, identifier);
            index++;
        }

        tag.putInt("consumedInputTypesCount", consumedInputs.keySet().size());
        index = 0;
        for (var typeClass : consumedInputs.keySet())
        {
            var identifier = IngredientManager.getIngredientHandlerIdentifier(typeClass).toString();
            tag.putString("consumedInputType" + index, identifier);

            consumedInputForTypeToTag(tag, typeClass, identifier);
            index++;
        }

        return tag;
    }

    @SuppressWarnings("unchecked")
    private <T> void consumedInputForTypeToTag(CompoundTag tag, Class<T> typeClass, String identifier)
    {
        var ingredientHandler = IngredientManager.getIngredientHandler(typeClass);
        var consumedInputsForType = (Collection<T>) consumedInputs.get(typeClass);

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
        var ingredientHandler = IngredientManager.getIngredientHandler(typeClass);
        var leftInputsForType = (Collection<T>) leftInputs.get(typeClass);

        tag.putInt("leftInputForType" + identifier, leftInputsForType.size());
        int index = 0;
        for (T input : leftInputsForType)
        {
            tag.put("leftInputForType" + identifier + index, ingredientHandler.toTag(input, new CompoundTag()));
            index++;
        }
    }

    public static <RECIPE extends RecipeBase> RecipeState fromTag(RecipeCategory<RECIPE> category, CompoundTag tag)
    {
        var recipe = category.getRecipe(ResourceLocation.tryBuild(tag.getString("recipeNamespace"), tag.getString("recipePath")));

        if (recipe.isEmpty())
            return null;

        var state = recipe.get().createState();
        state.fromTag(tag);
        return state;
    }

    @Override
    public void fromTag(CompoundTag tag)
    {
        consumedInputs.clear();
        leftInputs.clear();

        currentTime = tag.getInt("currentTime");
        recipeTime = tag.getInt("recipeTime");

        var leftInputTypesCount = tag.getInt("leftInputTypesCount");

        for (int index = 0; index < leftInputTypesCount; index++)
        {
            var identifier = tag.getString("leftInputType" + index);
            var handlerType = IngredientManager.getIngredientHandlerClass(new ResourceLocation(identifier));

            leftInputForTypeFromTag(tag, handlerType, identifier);
        }

        var consumedInputTypesCount = tag.getInt("consumedInputTypesCount");

        for (int index = 0; index < consumedInputTypesCount; index++)
        {
            var identifier = tag.getString("consumedInputType" + index);
            var handlerType = IngredientManager.getIngredientHandlerClass(new ResourceLocation(identifier));

            consumedInputForTypeFromTag(tag, handlerType, identifier);
        }
    }

    private <T> void leftInputForTypeFromTag(CompoundTag tag, Class<T> typeClass, String identifier)
    {
        var leftInputForType = tag.getInt("leftInputForType" + identifier);
        var ingredientHandler = IngredientManager.getIngredientHandler(typeClass);

        for (int index = 0; index < leftInputForType; index++)
            leftInputs.put(typeClass, ingredientHandler.fromTag(tag.getCompound("leftInputForType" + identifier + index)));
    }

    private <T> void consumedInputForTypeFromTag(CompoundTag tag, Class<T> typeClass, String identifier)
    {
        var consumedInputForType = tag.getInt("consumedInputForType" + identifier);
        var ingredientHandler = IngredientManager.getIngredientHandler(typeClass);

        for (int index = 0; index < consumedInputForType; index++)
            consumedInputs.put(typeClass, ingredientHandler.fromTag(tag.getCompound("consumedInputForType" + identifier + index)));
    }
}

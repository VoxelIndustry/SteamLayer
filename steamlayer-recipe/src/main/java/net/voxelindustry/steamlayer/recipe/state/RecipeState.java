package net.voxelindustry.steamlayer.recipe.state;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

public class RecipeState
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

    public void save(ValueOutput output)
    {
        output.putString("recipeNamespace", recipe.getId().getNamespace());
        output.putString("recipePath", recipe.getId().getPath());

        output.putInt("currentTime", currentTime);
        output.putInt("recipeTime", recipeTime);

        output.putInt("leftInputTypesCount", leftInputs.keySet().size());

        int index = 0;
        for (var typeClass : leftInputs.keySet())
        {
            var identifier = IngredientManager.getIngredientHandlerIdentifier(typeClass).toString();
            output.putString("leftInputType" + index, identifier);

            saveInputsForType(output, leftInputs, "leftInputForType", typeClass, identifier);
            index++;
        }

        output.putInt("consumedInputTypesCount", consumedInputs.keySet().size());
        index = 0;
        for (var typeClass : consumedInputs.keySet())
        {
            var identifier = IngredientManager.getIngredientHandlerIdentifier(typeClass).toString();
            output.putString("consumedInputType" + index, identifier);

            saveInputsForType(output, consumedInputs, "consumedInputForType", typeClass, identifier);
            index++;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void saveInputsForType(ValueOutput output, Multimap<Class<?>, ?> inputs, String prefix, Class<T> typeClass, String identifier)
    {
        var ingredientHandler = IngredientManager.getIngredientHandler(typeClass);
        var inputsForType = (Collection<T>) inputs.get(typeClass);

        output.putInt(prefix + identifier, inputsForType.size());
        int index = 0;
        for (T input : inputsForType)
        {
            ingredientHandler.save(input, output.child(prefix + identifier + index));
            index++;
        }
    }

    public static <RECIPE extends RecipeBase> RecipeState load(RecipeCategory<RECIPE> category, ValueInput input)
    {
        var recipeId = Identifier.tryBuild(input.getStringOr("recipeNamespace", ""), input.getStringOr("recipePath", ""));
        var recipe = recipeId == null ? Optional.<RECIPE>empty() : category.getRecipe(recipeId);

        if (recipe.isEmpty())
            return null;

        var state = recipe.get().createState();
        state.load(input);
        return state;
    }

    public void load(ValueInput input)
    {
        consumedInputs.clear();
        leftInputs.clear();

        currentTime = input.getIntOr("currentTime", 0);
        recipeTime = input.getIntOr("recipeTime", 0);

        var leftInputTypesCount = input.getIntOr("leftInputTypesCount", 0);

        for (int index = 0; index < leftInputTypesCount; index++)
        {
            var identifier = input.getStringOr("leftInputType" + index, "");
            var handlerType = IngredientManager.getIngredientHandlerClass(Identifier.parse(identifier));

            loadInputsForType(input, leftInputs, "leftInputForType", handlerType, identifier);
        }

        var consumedInputTypesCount = input.getIntOr("consumedInputTypesCount", 0);

        for (int index = 0; index < consumedInputTypesCount; index++)
        {
            var identifier = input.getStringOr("consumedInputType" + index, "");
            var handlerType = IngredientManager.getIngredientHandlerClass(Identifier.parse(identifier));

            loadInputsForType(input, consumedInputs, "consumedInputForType", handlerType, identifier);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void loadInputsForType(ValueInput input, Multimap<Class<?>, ?> inputs, String prefix, Class<T> typeClass, String identifier)
    {
        var count = input.getIntOr(prefix + identifier, 0);
        var ingredientHandler = IngredientManager.getIngredientHandler(typeClass);

        for (int index = 0; index < count; index++)
            ((Multimap<Class<?>, T>) inputs).put(typeClass, ingredientHandler.load(input.childOrEmpty(prefix + identifier + index)));
    }
}

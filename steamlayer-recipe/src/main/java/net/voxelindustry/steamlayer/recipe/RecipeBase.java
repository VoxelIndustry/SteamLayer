package net.voxelindustry.steamlayer.recipe;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.voxelindustry.steamlayer.recipe.ingredient.RecipeIngredient;
import net.voxelindustry.steamlayer.recipe.state.RecipeState;
import net.voxelindustry.steamlayer.recipe.vanilla.SteamLayerRecipeType;
import net.voxelindustry.steamlayer.recipe.vanilla.SteamLayerVanillaRecipeBridge;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class RecipeBase implements SteamLayerVanillaRecipeBridge
{
    private final SteamLayerRecipeType<? extends RecipeBase> recipeType;

    private Identifier identifier;

    protected final Multimap<Class<?>, RecipeIngredient<?>> inputs = Multimaps.newListMultimap(Maps.newIdentityHashMap(), ArrayList::new);
    protected final Multimap<Class<?>, RecipeIngredient<?>> outputs = Multimaps.newListMultimap(Maps.newIdentityHashMap(), ArrayList::new);

    protected RecipeCallback onCraft;

    protected RecipeBase(SteamLayerRecipeType<? extends RecipeBase> recipeType, Identifier identifier)
    {
        this.recipeType = recipeType;
        this.identifier = identifier;
    }

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
        return NonNullList.create();
    }

    @SuppressWarnings("unchecked")
    public <T> List<RecipeIngredient<T>> getRecipeOutputs(Class<T> clazz)
    {
        if (hasOutputType(clazz))
            return (List<RecipeIngredient<T>>) (Object) outputs.get(clazz);
        return NonNullList.create();
    }

    public <T> T getRecipeInput(Class<T> clazz, int slot)
    {
        List<RecipeIngredient<T>> recipeInputs = getRecipeInputs(clazz);

        if (recipeInputs.size() > slot)
            return recipeInputs.get(slot).getRaw();
        return IngredientManager.getIngredientHandler(clazz).getEmpty();
    }

    public <T> T getRecipeOutput(Class<T> clazz, int slot)
    {
        List<RecipeIngredient<T>> recipeOutputs = getRecipeOutputs(clazz);

        if (recipeOutputs.size() > slot)
            return recipeOutputs.get(slot).getRaw();
        return IngredientManager.getIngredientHandler(clazz).getEmpty();
    }

    public void onCraft(Level world, BlockPos pos, RecipeState state, LivingEntity crafter)
    {
        if (onCraft != null)
            onCraft.run(world, pos, state, crafter);
    }

    public Optional<List<ItemStack>> getRemainingItems()
    {
        return Optional.empty();
    }

    public abstract int getTime();

    public RecipeState createState()
    {
        return new RecipeState(
                this,
                Multimaps.transformValues(ArrayListMultimap.create(inputs), RecipeIngredient::staticForState),
                Multimaps.transformValues(ArrayListMultimap.create(outputs), RecipeIngredient::staticForState),
                getTime()
        );
    }

    public RecipeState createState(ValueInput existingData)
    {
        var state = createState();
        state.load(existingData);
        return state;
    }

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

    public void fromJson(JsonObject json)
    {
        RecipeSerializerHelper.ingredientMultimapFromJson(GsonHelper.getAsJsonObject(json, "inputs"), inputs);
        RecipeSerializerHelper.ingredientMultimapFromJson(GsonHelper.getAsJsonObject(json, "outputs"), outputs);
    }

    public void toJson(JsonObject json)
    {
        json.add("inputs", RecipeSerializerHelper.ingredientMultimapToJson(inputs));
        json.add("outputs", RecipeSerializerHelper.ingredientMultimapToJson(outputs));
    }

    public void fromByteBuf(RegistryFriendlyByteBuf buffer)
    {
        RecipeSerializerHelper.ingredientMultimapFromByteBuf(buffer, inputs);
        RecipeSerializerHelper.ingredientMultimapFromByteBuf(buffer, outputs);
    }

    public void toByteBuf(RegistryFriendlyByteBuf buffer)
    {
        RecipeSerializerHelper.ingredientMultimapToByteBuf(buffer, inputs);
        RecipeSerializerHelper.ingredientMultimapToByteBuf(buffer, outputs);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<RecipeBase> getSerializer()
    {
        return (RecipeSerializer<RecipeBase>) recipeType.getSerializer();
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeType<RecipeBase> getType()
    {
        return (RecipeType<RecipeBase>) recipeType;
    }

    public Identifier getId()
    {
        return identifier;
    }

    /**
     * Recipes decoded from datapacks do not know their identifier, it is assigned by SteamLayer once they are loaded.
     */
    @ApiStatus.Internal
    public void setId(Identifier identifier)
    {
        this.identifier = identifier;
    }

    @Override
    public ItemStack assemble(RecipeInput input)
    {
        return getRecipeOutput(ItemStack.class, 0).copy();
    }
}

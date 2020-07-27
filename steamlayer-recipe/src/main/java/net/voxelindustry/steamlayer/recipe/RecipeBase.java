package net.voxelindustry.steamlayer.recipe;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.voxelindustry.steamlayer.recipe.ingredient.RecipeIngredient;
import net.voxelindustry.steamlayer.recipe.state.RecipeState;
import net.voxelindustry.steamlayer.recipe.vanilla.SteamLayerRecipeType;
import net.voxelindustry.steamlayer.recipe.vanilla.SteamLayerVanillaRecipeBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class RecipeBase implements SteamLayerVanillaRecipeBridge
{
    private final SteamLayerRecipeType<? extends RecipeBase> recipeType;

    private final Identifier identifier;

    protected final Multimap<Class<?>, RecipeIngredient<?>> inputs  = Multimaps.newListMultimap(Maps.newIdentityHashMap(), ArrayList::new);
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
        return DefaultedList.of();
    }

    @SuppressWarnings("unchecked")
    public <T> List<RecipeIngredient<T>> getRecipeOutputs(Class<T> clazz)
    {
        if (hasOutputType(clazz))
            return (List<RecipeIngredient<T>>) (Object) outputs.get(clazz);
        return DefaultedList.of();
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

    public void onCraft(World world, BlockPos pos, RecipeState state, LivingEntity crafter)
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

    public RecipeState createState(CompoundTag existingData)
    {
        RecipeState state = createState();
        state.fromTag(existingData);
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
        RecipeSerializerHelper.ingredientMultimapFromJson(JsonHelper.getObject(json, "inputs"), inputs);
        RecipeSerializerHelper.ingredientMultimapFromJson(JsonHelper.getObject(json, "outputs"), outputs);
    }

    public void fromByteBuf(PacketByteBuf buffer)
    {
        RecipeSerializerHelper.ingredientMultimapFromByteBuf(buffer, inputs);
        RecipeSerializerHelper.ingredientMultimapFromByteBuf(buffer, outputs);
    }

    public void toByteBuf(PacketByteBuf buffer)
    {
        RecipeSerializerHelper.ingredientMultimapToByteBuf(buffer, inputs);
        RecipeSerializerHelper.ingredientMultimapToByteBuf(buffer, outputs);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<RecipeBase> getSerializer()
    {
        return (RecipeSerializer<RecipeBase>) recipeType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeType<RecipeBase> getType()
    {
        return (RecipeType<RecipeBase>) recipeType;
    }

    @Override
    public Identifier getId()
    {
        return identifier;
    }
}

package net.voxelindustry.steamlayer.recipe.vanilla;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.voxelindustry.steamlayer.recipe.RecipeBase;

import java.util.function.BiFunction;

/**
 * Recipe type of SteamLayer recipes, also holding their serializer.
 * Recipes keep their JSON format: the codec delegates to {@link RecipeBase#fromJson(JsonObject)} and {@link RecipeBase#toJson(JsonObject)}.
 * The identifier given to the recipe creator is null when decoding, it is assigned once the recipe is loaded.
 */
public class SteamLayerRecipeType<R extends RecipeBase> implements RecipeType<R>
{
    private final BiFunction<SteamLayerRecipeType<R>, Identifier, R> recipeCreator;

    @Getter
    private final RecipeSerializer<R> serializer;

    public SteamLayerRecipeType(BiFunction<SteamLayerRecipeType<R>, Identifier, R> recipeCreator)
    {
        this.recipeCreator = recipeCreator;

        MapCodec<R> codec = MapCodec.assumeMapUnsafe(Codec.PASSTHROUGH.comapFlatMap(this::decode, this::encode));
        StreamCodec<RegistryFriendlyByteBuf, R> streamCodec = StreamCodec.of((buffer, recipe) -> recipe.toByteBuf(buffer), this::fromNetwork);
        serializer = new RecipeSerializer<>(codec, streamCodec);
    }

    public R create(Identifier id)
    {
        return recipeCreator.apply(this, id);
    }

    private DataResult<R> decode(Dynamic<?> input)
    {
        try
        {
            R recipe = create(null);
            recipe.fromJson(input.convert(JsonOps.INSTANCE).getValue().getAsJsonObject());
            return DataResult.success(recipe);
        } catch (RuntimeException e)
        {
            return DataResult.error(() -> "Invalid SteamLayer recipe: " + e.getMessage());
        }
    }

    private Dynamic<?> encode(R recipe)
    {
        JsonObject json = new JsonObject();
        recipe.toJson(json);
        return new Dynamic<>(JsonOps.INSTANCE, json);
    }

    private R fromNetwork(RegistryFriendlyByteBuf buffer)
    {
        R recipe = create(null);
        recipe.fromByteBuf(buffer);
        return recipe;
    }
}

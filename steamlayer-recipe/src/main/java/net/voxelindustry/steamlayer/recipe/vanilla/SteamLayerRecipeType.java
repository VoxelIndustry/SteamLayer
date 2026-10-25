package net.voxelindustry.steamlayer.recipe.vanilla;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.voxelindustry.steamlayer.recipe.RecipeBase;

import java.util.function.BiFunction;

public class SteamLayerRecipeType<R extends RecipeBase> implements RecipeType<R>, RecipeSerializer<R>
{
    private final BiFunction<SteamLayerRecipeType<R>, ResourceLocation, R> recipeCreator;

    public SteamLayerRecipeType(BiFunction<SteamLayerRecipeType<R>, ResourceLocation, R> recipeCreator)
    {
        this.recipeCreator = recipeCreator;
    }

    @Override
    public R fromJson(ResourceLocation id, JsonObject json)
    {
        R createdRecipe = recipeCreator.apply(this, id);
        createdRecipe.fromJson(json);
        return createdRecipe;
    }

    @Override
    public R fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
    {
        R createdRecipe = recipeCreator.apply(this, id);
        createdRecipe.fromByteBuf(buf);
        return createdRecipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, R recipe)
    {
        recipe.toByteBuf(buf);
    }
}

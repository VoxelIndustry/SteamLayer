package net.voxelindustry.steamlayer.recipe.vanilla;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.voxelindustry.steamlayer.recipe.RecipeBase;

import java.util.function.BiFunction;

public class SteamLayerRecipeType<R extends RecipeBase> implements RecipeType<R>, RecipeSerializer<R>
{
    private final BiFunction<SteamLayerRecipeType<R>, Identifier, R> recipeCreator;

    public SteamLayerRecipeType(BiFunction<SteamLayerRecipeType<R>, Identifier, R> recipeCreator)
    {
        this.recipeCreator = recipeCreator;
    }

    @Override
    public R read(Identifier id, JsonObject json)
    {
        R createdRecipe = recipeCreator.apply(this, id);
        createdRecipe.fromJson(json);
        return createdRecipe;
    }

    @Override
    public R read(Identifier id, PacketByteBuf buf)
    {
        R createdRecipe = recipeCreator.apply(this, id);
        createdRecipe.fromByteBuf(buf);
        return createdRecipe;
    }

    @Override
    public void write(PacketByteBuf buf, R recipe)
    {
        recipe.toByteBuf(buf);
    }
}

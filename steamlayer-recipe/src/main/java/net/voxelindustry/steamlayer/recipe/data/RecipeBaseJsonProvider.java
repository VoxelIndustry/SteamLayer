package net.voxelindustry.steamlayer.recipe.data;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.voxelindustry.steamlayer.recipe.RecipeBase;
import net.voxelindustry.steamlayer.recipe.vanilla.SteamLayerRecipeType;
import org.jetbrains.annotations.Nullable;

public class RecipeBaseJsonProvider implements FinishedRecipe
{
    private final RecipeBase recipe;
    private final SteamLayerRecipeType<?> type;

    private final Advancement.Builder advancementBuilder;
    private final ResourceLocation advancementId;

    public RecipeBaseJsonProvider(RecipeBase recipe, SteamLayerRecipeType<?> type, Advancement.Builder advancementBuilder, ResourceLocation advancementId)
    {
        this.recipe = recipe;
        this.type = type;
        this.advancementBuilder = advancementBuilder;
        this.advancementId = advancementId;
    }

    @Override
    public void serializeRecipeData(JsonObject json)
    {
        recipe.toJson(json);
    }

    @Override
    public ResourceLocation getId()
    {
        return recipe.getId();
    }

    @Override
    public RecipeSerializer<?> getType()
    {
        return type;
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement()
    {
        return this.advancementBuilder.serializeToJson();
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId()
    {
        return this.advancementId;
    }
}

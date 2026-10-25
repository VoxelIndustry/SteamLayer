package net.voxelindustry.steamlayer.recipe.data;

import com.google.gson.JsonObject;
import net.minecraft.advancement.Advancement;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.voxelindustry.steamlayer.recipe.RecipeBase;
import net.voxelindustry.steamlayer.recipe.vanilla.SteamLayerRecipeType;
import org.jetbrains.annotations.Nullable;

public class RecipeBaseJsonProvider implements RecipeJsonProvider
{
    private final RecipeBase recipe;
    private final SteamLayerRecipeType<?> type;

    private final Advancement.Builder advancementBuilder;
    private final Identifier advancementId;

    public RecipeBaseJsonProvider(RecipeBase recipe, SteamLayerRecipeType<?> type, Advancement.Builder advancementBuilder, Identifier advancementId)
    {
        this.recipe = recipe;
        this.type = type;
        this.advancementBuilder = advancementBuilder;
        this.advancementId = advancementId;
    }

    @Override
    public void serialize(JsonObject json)
    {
        recipe.toJson(json);
    }

    @Override
    public Identifier getRecipeId()
    {
        return recipe.getId();
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return type;
    }

    @Nullable
    @Override
    public JsonObject toAdvancementJson()
    {
        return this.advancementBuilder.toJson();
    }

    @Nullable
    @Override
    public Identifier getAdvancementId()
    {
        return this.advancementId;
    }
}

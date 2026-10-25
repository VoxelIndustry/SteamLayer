package net.voxelindustry.steamlayer.recipe.data;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;
import net.voxelindustry.steamlayer.recipe.RecipeBase;
import net.voxelindustry.steamlayer.recipe.vanilla.SteamLayerRecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RecipeBaseJsonBuilder implements CraftingRecipeJsonBuilder
{
    private final Advancement.Builder advancementBuilder = Advancement.Builder.createUntelemetered();

    private final RecipeBase recipe;
    private final SteamLayerRecipeType<?> serializer;
    private final RecipeCategory category;

    public RecipeBaseJsonBuilder(RecipeBase recipe, SteamLayerRecipeType<?> serializer, RecipeCategory category)
    {
        this.recipe = recipe;
        this.serializer = serializer;
        this.category = category;
    }

    @Override
    public CraftingRecipeJsonBuilder criterion(String name, CriterionConditions conditions)
    {
        this.advancementBuilder.criterion(name, conditions);
        return this;
    }

    @Override
    public CraftingRecipeJsonBuilder group(@Nullable String group)
    {
        // Unused
        // Unsure of its use in vanilla
        return this;
    }

    @Override
    public Item getOutputItem()
    {
        if (recipe.hasOutputType(ItemStack.class))
            return recipe.getRecipeOutput(ItemStack.class, 0).getItem();
        return Items.AIR;
    }

    @Override
    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier recipeId)
    {
        this.validate(recipeId);
        this.advancementBuilder.parent(ROOT).criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).criteriaMerger(CriterionMerger.OR);
        exporter.accept(new RecipeBaseJsonProvider(recipe, this.serializer, this.advancementBuilder, recipeId.withPrefixedPath("recipes/" + this.category.getName() + "/")));
    }

    private void validate(Identifier recipeId)
    {
        if (this.advancementBuilder.getCriteria().isEmpty())
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
    }
}

package net.voxelindustry.steamlayer.recipe.data;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.voxelindustry.steamlayer.recipe.RecipeBase;
import net.voxelindustry.steamlayer.recipe.vanilla.SteamLayerRecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RecipeBaseJsonBuilder implements RecipeBuilder
{
    private final Advancement.Builder advancementBuilder = Advancement.Builder.recipeAdvancement();

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
    public RecipeBuilder unlockedBy(String name, CriterionTriggerInstance conditions)
    {
        this.advancementBuilder.addCriterion(name, conditions);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group)
    {
        // Unused
        // Unsure of its use in vanilla
        return this;
    }

    @Override
    public Item getResult()
    {
        if (recipe.hasOutputType(ItemStack.class))
            return recipe.getRecipeOutput(ItemStack.class, 0).getItem();
        return Items.AIR;
    }

    @Override
    public void save(Consumer<FinishedRecipe> exporter, ResourceLocation recipeId)
    {
        this.validate(recipeId);
        this.advancementBuilder.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId)).rewards(AdvancementRewards.Builder.recipe(recipeId)).requirements(RequirementsStrategy.OR);
        exporter.accept(new RecipeBaseJsonProvider(recipe, this.serializer, this.advancementBuilder, recipeId.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void validate(ResourceLocation recipeId)
    {
        if (this.advancementBuilder.getCriteria().isEmpty())
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
    }
}

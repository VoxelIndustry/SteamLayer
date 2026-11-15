package net.voxelindustry.steamlayer.recipe.data;

import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.voxelindustry.steamlayer.recipe.RecipeBase;
import org.jspecify.annotations.Nullable;

/**
 * Datagen builder for SteamLayer recipes. The recipe is written through its type codec, keeping the SteamLayer JSON format.
 */
public class RecipeBaseBuilder implements RecipeBuilder
{
    private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();

    private final RecipeBase     recipe;
    private final RecipeCategory category;

    public RecipeBaseBuilder(RecipeBase recipe, RecipeCategory category)
    {
        this.recipe = recipe;
        this.category = category;
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion)
    {
        advancementBuilder.unlockedBy(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group)
    {
        return this;
    }

    @Override
    public @Nullable ResourceKey<Recipe<?>> defaultId()
    {
        if (recipe.hasOutputType(ItemStack.class) && !recipe.getRecipeOutput(ItemStack.class, 0).isEmpty())
            return RecipeBuilder.getDefaultRecipeId(recipe.getRecipeOutput(ItemStack.class, 0));
        return null;
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> id)
    {
        recipe.setId(id.identifier());
        output.accept(id, recipe, advancementBuilder.build(output, id, category));
    }
}

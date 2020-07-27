package net.voxelindustry.steamlayer.recipe;

import net.voxelindustry.steamlayer.recipe.category.RecipeCategory;

@FunctionalInterface
public interface RecipeCategoryReloadEvent
{
    void recipeReload(RecipeCategory<?> category);
}

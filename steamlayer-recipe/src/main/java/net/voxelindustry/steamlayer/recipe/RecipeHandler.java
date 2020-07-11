package net.voxelindustry.steamlayer.recipe;

import net.minecraft.util.Identifier;
import net.voxelindustry.steamlayer.recipe.category.RecipeCategory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class RecipeHandler
{
    private static final HashMap<Identifier, RecipeCategory> RECIPES = new HashMap<>();

    public static RecipeCategory addCategory(RecipeCategory category)
    {
        RECIPES.put(category.getIdentifier(), category);
        return category;
    }

    public static RecipeCategory getCategory(Identifier category)
    {
        return RECIPES.get(category);
    }

    /**
     * Check if a given indexed ingredient match a recipe.
     * Only shaped recipes are currently supported.
     * The matching logic is deferred to the <code>RecipeCategory</code> which by default implementation
     * will defer this logic to the <code>RecipeIngredient</code> wrapper after retrieving the specified index.
     * This method does not take into account the quantity of the ingredient.
     *
     * @param recipeID   of the category to check
     * @param recipeSlot of the ingredient
     * @param ingredient to match
     * @param <T>        a generic ingredient that can be wrapped by the recipe in a <code>RecipeIngredient</code>
     *                   instance
     * @return the result of the match
     */
    public static <T> boolean inputMatchWithoutCount(String recipeID, int recipeSlot, T ingredient)
    {
        RecipeCategory category = RecipeHandler.RECIPES.get(recipeID);
        return category != null && category.inputMatchWithoutCount(recipeSlot, ingredient);
    }

    /**
     * Check if a given indexed ingredient match a recipe.
     * Only shaped recipes are currently supported.
     * The matching logic is deferred to the <code>RecipeCategory</code> which by default implementation
     * will defer this logic to the <code>RecipeIngredient</code> wrapper after retrieving the specified index.
     * This method does take into account the quantity of the ingredient,
     * it must be at least equals or superior to the recipe expected quantity.
     *
     * @param recipeID   of the category to check
     * @param recipeSlot of the ingredient
     * @param ingredient to match
     * @param <T>        a generic ingredient that can be wrapped by the recipe in a <code>RecipeIngredient</code>
     *                   instance
     * @return the result of the match
     */
    public static <T> boolean inputMatchWithCount(String recipeID, int recipeSlot, T ingredient)
    {
        RecipeCategory category = RecipeHandler.RECIPES.get(recipeID);
        return category != null && category.inputMatchWithCount(recipeSlot, ingredient);
    }

    /**
     * Retrieve a specific <code>RecipeBase</code> from the category containing it and a list of generic ingredients.
     * By default implementation the <code>RecipeCategory</code> will attempt to match the list of ingredients
     * against all its registered recipes.
     * Many custom categories will return a fake recipe for dynamic evaluation.
     * <p>
     * This method has a high performance cost and its return should be always cached.
     *
     * @param recipeID of the category to check
     * @param inputs   an index-ordered list of arbitrary generic ingredients
     * @return an Optional containing the found recipe or null in case of failure
     */
    public static Optional<RecipeBase> getRecipe(String recipeID, Object... inputs)
    {
        RecipeCategory category = RecipeHandler.RECIPES.get(recipeID);
        if (category != null)
            return category.getRecipe(inputs);
        return Optional.empty();
    }

    public static List<RecipeBase> getRecipesLike(String recipeID, Object... inputs)
    {
        RecipeCategory category = RecipeHandler.RECIPES.get(recipeID);
        if (category != null)
            return RecipeHandler.RECIPES.get(recipeID).getRecipesLike(inputs);
        return Collections.emptyList();
    }
}

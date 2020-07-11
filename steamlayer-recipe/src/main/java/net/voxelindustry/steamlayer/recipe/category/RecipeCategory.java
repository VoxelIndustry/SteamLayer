package net.voxelindustry.steamlayer.recipe.category;

import lombok.Getter;
import net.voxelindustry.steamlayer.recipe.RecipeBase;
import net.voxelindustry.steamlayer.recipe.ingredient.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class RecipeCategory
{
    private String           name;
    private List<RecipeBase> recipes;

    public RecipeCategory(String name)
    {
        this.name = name;

        recipes = new ArrayList<>();
    }

    public <T> boolean inputMatchWithoutCount(int recipeSlot, T ingredient)
    {
        return recipes.stream().anyMatch(recipe ->
        {
            if (!recipe.hasInputType(ingredient.getClass())
                    || recipe.getRecipeInputs(ingredient.getClass()).size() < recipeSlot)
                return false;
            return ((RecipeIngredient<T>) recipe.getRecipeInputs(ingredient.getClass()).get(recipeSlot))
                    .match(ingredient);
        });
    }

    public <T> boolean inputMatchWithCount(int recipeSlot, T ingredient)
    {
        return recipes.stream().anyMatch(recipe ->
        {
            if (!recipe.hasInputType(ingredient.getClass())
                    || recipe.getRecipeInputs(ingredient.getClass()).size() < recipeSlot)
                return false;
            return ((RecipeIngredient<T>) recipe.getRecipeInputs(ingredient.getClass()).get(recipeSlot))
                    .matchWithQuantity(ingredient);
        });
    }

    public Optional<RecipeBase> getRecipe(Object... inputs)
    {
        return recipes.stream().filter(recipe ->
        {
            int i = 0;
            for (Object ingredient : inputs)
            {
                if (!recipe.hasInputType(ingredient.getClass())
                        || i >= recipe.getRecipeInputs(ingredient.getClass()).size())
                    break;
                if (!((RecipeIngredient<Object>) recipe.getRecipeInputs(ingredient.getClass()).get(i))
                        .matchWithQuantity(ingredient))
                    return false;
                i++;
            }
            return true;
        }).findFirst();
    }

    public List<RecipeBase> getRecipesLike(Object... inputs)
    {
        return recipes.stream().filter(recipe ->
        {
            for (Object ingredient : inputs)
            {
                if (!recipe.hasInputType(ingredient.getClass()))
                    break;
                if (recipe.getRecipeInputs(ingredient.getClass()).stream().noneMatch(recipeIngredient ->
                        ((RecipeIngredient<Object>) recipeIngredient).match(ingredient)))
                    return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    public void add(RecipeBase recipe)
    {
        recipes.add(recipe);
    }
}

package net.voxelindustry.steamlayer.recipe.category;

import lombok.Getter;
import net.minecraft.util.Identifier;
import net.voxelindustry.steamlayer.recipe.RecipeBase;
import net.voxelindustry.steamlayer.recipe.ingredient.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Getter
public class RecipeCategory
{
    private final Identifier       identifier;
    private final List<RecipeBase> recipes = new ArrayList<>();

    public RecipeCategory(Identifier identifier)
    {
        this.identifier = identifier;
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
    public Stream<RecipeBase> findRecipes(Object... inputs)
    {
        return recipes.stream().filter(recipe ->
        {
            int i = 0;
            for (Object ingredient : inputs)
            {
                if (!recipe.hasInputType(ingredient.getClass()))
                    break;
                if (i >= recipe.getRecipeInputs(ingredient.getClass()).size())
                    break;
                if (!((RecipeIngredient<Object>) recipe.getRecipeInputs(ingredient.getClass()).get(i)).matchWithQuantity(ingredient))
                    return false;
                i++;
            }
            return true;
        });
    }

    public Optional<RecipeBase> findOneRecipe(Object... inputs)
    {
        return findRecipes(inputs).findFirst();
    }

    @SuppressWarnings("unchecked")
    public Stream<RecipeBase> findRecipesWithoutCount(Object... inputs)
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
        });
    }

    public void add(RecipeBase recipe)
    {
        recipes.add(recipe);
    }
}

package net.voxelindustry.steamlayer.recipe.category;

import lombok.Getter;
import net.minecraft.util.Identifier;
import net.voxelindustry.steamlayer.recipe.RecipeBase;
import net.voxelindustry.steamlayer.recipe.RecipeManager;
import net.voxelindustry.steamlayer.recipe.ingredient.RecipeIngredient;
import net.voxelindustry.steamlayer.recipe.vanilla.SteamLayerRecipeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static net.voxelindustry.steamlayer.recipe.SteamLayerRecipe.RECIPE_CATEGORY_RELOAD_EVENT;

@Getter
public class RecipeCategory<R extends RecipeBase>
{
    private final Identifier              identifier;
    private final SteamLayerRecipeType<R> type;

    private final List<R> recipes        = new ArrayList<>();
    private final List<R> vanillaRecipes = new ArrayList<>();

    public RecipeCategory(Identifier identifier, SteamLayerRecipeType<R> type)
    {
        this.identifier = identifier;
        this.type = type;

        if (type != null)
            RecipeManager.addRecipeType(type, identifier);
    }

    @SuppressWarnings("unchecked")
    public <T> boolean inputMatchWithoutCount(int recipeSlot, T ingredient)
    {
        return Stream.concat(recipes.stream(), vanillaRecipes.stream()).anyMatch(recipe ->
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
        return Stream.concat(recipes.stream(), vanillaRecipes.stream()).anyMatch(recipe ->
        {
            if (!recipe.hasInputType(ingredient.getClass())
                    || recipe.getRecipeInputs(ingredient.getClass()).size() < recipeSlot)
                return false;
            return ((RecipeIngredient<T>) recipe.getRecipeInputs(ingredient.getClass()).get(recipeSlot))
                    .matchWithQuantity(ingredient);
        });
    }

    @SuppressWarnings("unchecked")
    public Stream<R> findRecipes(Object... inputs)
    {
        return Stream.concat(recipes.stream(), vanillaRecipes.stream()).filter(recipe ->
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

    public Optional<R> findOneRecipe(Object... inputs)
    {
        return findRecipes(inputs).findFirst();
    }

    @SuppressWarnings("unchecked")
    public Stream<R> findRecipesWithoutCount(Object... inputs)
    {
        return Stream.concat(recipes.stream(), vanillaRecipes.stream()).filter(recipe ->
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

    public void add(R recipe)
    {
        recipes.add(recipe);
    }

    public Optional<R> getRecipe(Identifier identifier)
    {
        return Stream.concat(recipes.stream(), vanillaRecipes.stream())
                .filter(recipe -> recipe.getId().equals(identifier)).findFirst();
    }

    public void replaceVanillaRecipes(Collection<R> recipes)
    {
        vanillaRecipes.clear();
        vanillaRecipes.addAll(recipes);

        RECIPE_CATEGORY_RELOAD_EVENT.invoker().recipeReload(this);
    }
}

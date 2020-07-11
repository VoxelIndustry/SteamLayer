package net.voxelindustry.steamlayer.recipe.category;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import net.voxelindustry.steamlayer.recipe.RecipeBase;
import net.voxelindustry.steamlayer.recipe.ingredient.ItemStackRecipeIngredient;
import net.voxelindustry.steamlayer.test.SteamLayerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SteamLayerExtension.class)
public class RecipeCategoryTest
{
    @Test
    void getRecipe_givenPartialRecipeMatch_thenShouldReturnRecipe()
    {
        RecipeCategory category = new RecipeCategory(null);
        RecipeBase recipe = mock(RecipeBase.class);
        category.add(recipe);

        ItemStack apple = new ItemStack(Items.APPLE);
        ItemStack potato = new ItemStack(Items.POTATO);
        when(recipe.getRecipeInputs(ItemStack.class)).thenReturn(DefaultedList.copyOf(new ItemStackRecipeIngredient(ItemStack.EMPTY), new ItemStackRecipeIngredient(apple), new ItemStackRecipeIngredient(potato)));
        when(recipe.hasInputType(ItemStack.class)).thenReturn(Boolean.TRUE);

        RecipeBase recipe2 = mock(RecipeBase.class);
        category.add(recipe2);

        when(recipe2.getRecipeInputs(ItemStack.class)).thenReturn(DefaultedList.copyOf(new ItemStackRecipeIngredient(ItemStack.EMPTY), new ItemStackRecipeIngredient(potato)));
        when(recipe2.hasInputType(ItemStack.class)).thenReturn(Boolean.TRUE);

        assertThat(category.findRecipes(apple)).contains(recipe);
    }
}

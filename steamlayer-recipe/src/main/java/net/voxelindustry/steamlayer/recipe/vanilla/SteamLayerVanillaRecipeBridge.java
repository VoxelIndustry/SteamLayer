package net.voxelindustry.steamlayer.recipe.vanilla;

import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

public interface SteamLayerVanillaRecipeBridge extends Recipe<RecipeInput>
{
    @Deprecated
    @Override
    default boolean matches(RecipeInput input, Level level)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isSpecial()
    {
        return true;
    }

    @Override
    default boolean showNotification()
    {
        return false;
    }

    @Override
    default String group()
    {
        return "";
    }

    @Override
    default PlacementInfo placementInfo()
    {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    default RecipeBookCategory recipeBookCategory()
    {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}

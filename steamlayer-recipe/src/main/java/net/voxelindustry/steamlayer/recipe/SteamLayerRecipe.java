package net.voxelindustry.steamlayer.recipe;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.voxelindustry.steamlayer.recipe.category.RecipeCategory;
import net.voxelindustry.steamlayer.recipe.mixin.AccessorRecipeManager;
import net.voxelindustry.steamlayer.recipe.vanilla.SteamLayerRecipeType;

import java.util.Collection;

public class SteamLayerRecipe implements ModInitializer
{
    public static final Event<RecipeCategoryReloadEvent> RECIPE_CATEGORY_RELOAD_EVENT = EventFactory.createArrayBacked(RecipeCategoryReloadEvent.class,
            callbacks -> category ->
            {
                for (RecipeCategoryReloadEvent callback : callbacks)
                    callback.recipeReload(category);
            });

    @Override
    public void onInitialize()
    {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> recipeLoad((AccessorRecipeManager) server.getRecipeManager()));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, dataPackContents, success) -> dataPackReload(server.getRecipeManager(), success));
    }

    private void dataPackReload(net.minecraft.recipe.RecipeManager recipeManager, boolean success)
    {
        if (success)
            recipeLoad((AccessorRecipeManager) recipeManager);
    }

    private void recipeLoad(AccessorRecipeManager recipeManager)
    {
        Collection<RecipeCategory<?>> categories = RecipeManager.getCategories();

        categories.forEach(category -> category.replaceVanillaRecipes(getRecipes(recipeManager, category.getType())));
    }

    @SuppressWarnings("unchecked")
    private <T extends RecipeBase> Collection<T> getRecipes(AccessorRecipeManager recipeManager, SteamLayerRecipeType<?> type)
    {
        return (Collection<T>) (Object) recipeManager.getAll(type).values();
    }
}

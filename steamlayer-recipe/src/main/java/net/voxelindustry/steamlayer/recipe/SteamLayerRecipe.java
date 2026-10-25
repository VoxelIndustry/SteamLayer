package net.voxelindustry.steamlayer.recipe;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.voxelindustry.steamlayer.recipe.category.RecipeCategory;

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
        ServerLifecycleEvents.SERVER_STARTING.register(server -> recipeLoad(server.getRecipeManager()));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) ->
        {
            if (success)
                recipeLoad(server.getRecipeManager());
        });
    }

    private void recipeLoad(net.minecraft.world.item.crafting.RecipeManager recipeManager)
    {
        RecipeManager.getCategories().forEach(category -> loadCategory(recipeManager, category));
    }

    @SuppressWarnings("unchecked")
    private <R extends RecipeBase> void loadCategory(net.minecraft.world.item.crafting.RecipeManager recipeManager, RecipeCategory<R> category)
    {
        Collection<R> recipes = recipeManager.getRecipes().stream()
                .filter(holder -> holder.value().getType() == category.getType())
                .map(holder ->
                {
                    var recipe = (R) holder.value();
                    recipe.setId(holder.id().identifier());
                    return recipe;
                })
                .toList();

        category.replaceVanillaRecipes(recipes);
    }
}

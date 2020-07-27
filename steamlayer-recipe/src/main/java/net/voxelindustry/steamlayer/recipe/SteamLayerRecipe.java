package net.voxelindustry.steamlayer.recipe;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
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
        SERVER_STARTING.register(server -> recipeLoad((AccessorRecipeManager) server.getRecipeManager()));
        END_DATA_PACK_RELOAD.register((serverResourceManager, success, success2) -> dataPackReload(success, success2));
    }

    private void dataPackReload(ServerResourceManager serverResourceManager, boolean success)
    {
        if (success)
            recipeLoad((AccessorRecipeManager) serverResourceManager.getRecipeManager());
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

    //FIXME: Backported to 1.16 until 1.16.2 stable release

    public static final Event<EndDataPackReload> END_DATA_PACK_RELOAD = EventFactory.createArrayBacked(EndDataPackReload.class, callbacks -> (server, serverResourceManager, success) ->
    {
        for (EndDataPackReload callback : callbacks)
        {
            callback.endDataPackReload(server, serverResourceManager, success);
        }
    });

    public interface EndDataPackReload
    {
        void endDataPackReload(MinecraftServer server, ServerResourceManager serverResourceManager, boolean success);
    }

    public static final Event<ServerStarting> SERVER_STARTING = EventFactory.createArrayBacked(ServerStarting.class, callbacks -> server ->
    {
        for (ServerStarting callback : callbacks)
        {
            callback.onServerStarting(server);
        }
    });

    public interface ServerStarting
    {
        void onServerStarting(MinecraftServer server);
    }
}

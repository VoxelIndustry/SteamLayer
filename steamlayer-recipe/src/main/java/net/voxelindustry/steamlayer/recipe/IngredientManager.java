package net.voxelindustry.steamlayer.recipe;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.voxelindustry.steamlayer.recipe.ingredient.IngredientHandler;
import net.voxelindustry.steamlayer.recipe.ingredient.ItemStackIngredientHandler;
import net.voxelindustry.steamlayer.recipe.ingredient.ItemStackRandRecipeIngredient;
import net.voxelindustry.steamlayer.recipe.ingredient.ItemStackRecipeIngredient;
import net.voxelindustry.steamlayer.recipe.ingredient.RecipeIngredient;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public class IngredientManager
{
    private static final Map<Class<?>, IngredientHandler<?>> INGREDIENT_HANDLERS = new IdentityHashMap<>();
    private static final BiMap<Identifier, Class<?>>         INGREDIENT_TYPES    = HashBiMap.create();

    private static final Map<Identifier, Function<PacketByteBuf, RecipeIngredient<?>>> INGREDIENT_INSTANCE_FROM_BUFFER = new HashMap<>();
    private static final Map<Identifier, Function<JsonObject, RecipeIngredient<?>>>    INGREDIENT_INSTANCE_FROM_JSON   = new HashMap<>();

    static
    {
        Identifier itemStackIdentifier = new Identifier("minecraft", "itemstack");
        INGREDIENT_TYPES.put(itemStackIdentifier, ItemStack.class);
        INGREDIENT_HANDLERS.put(ItemStack.class, new ItemStackIngredientHandler(itemStackIdentifier));

        INGREDIENT_INSTANCE_FROM_BUFFER.put(ItemStackRecipeIngredient.ITEMSTACK_INGREDIENT, ItemStackRecipeIngredient::fromBuffer);
        INGREDIENT_INSTANCE_FROM_BUFFER.put(ItemStackRandRecipeIngredient.ITEMSTACK_RAND_INGREDIENT, ItemStackRandRecipeIngredient::fromBuffer);

        INGREDIENT_INSTANCE_FROM_JSON.put(ItemStackRecipeIngredient.ITEMSTACK_INGREDIENT, ItemStackRecipeIngredient::fromJson);
        INGREDIENT_INSTANCE_FROM_JSON.put(ItemStackRandRecipeIngredient.ITEMSTACK_RAND_INGREDIENT, ItemStackRandRecipeIngredient::fromJson);
    }

    public static <T> IngredientHandler<T> addIngredientHandler(Class<T> ingredientType, IngredientHandler<T> ingredientHandler)
    {
        INGREDIENT_HANDLERS.put(ingredientType, ingredientHandler);
        INGREDIENT_TYPES.put(ingredientHandler.getIdentifier(), ingredientType);
        return ingredientHandler;
    }

    public static RecipeIngredient<?> getIngredientInstanceFromJSON(Identifier identifier, JsonObject object)
    {
        return INGREDIENT_INSTANCE_FROM_JSON.get(identifier).apply(object);
    }

    public static RecipeIngredient<?> getIngredientInstanceFromBuffer(Identifier identifier, PacketByteBuf buffer)
    {
        return INGREDIENT_INSTANCE_FROM_BUFFER.get(identifier).apply(buffer);
    }

    public static <T> Identifier getIngredientHandlerIdentifier(Class<T> ingredientType)
    {
        return INGREDIENT_TYPES.inverse().get(ingredientType);
    }

    public static Class<?> getIngredientHandlerClass(Identifier identifier)
    {
        return INGREDIENT_TYPES.get(identifier);
    }

    @SuppressWarnings("unchecked")
    public static <T> IngredientHandler<T> getIngredientHandler(Class<T> ingredientType)
    {
        return (IngredientHandler<T>) INGREDIENT_HANDLERS.get(ingredientType);
    }
}

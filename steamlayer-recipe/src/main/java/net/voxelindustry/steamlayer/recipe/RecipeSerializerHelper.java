package net.voxelindustry.steamlayer.recipe;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.voxelindustry.steamlayer.recipe.ingredient.RecipeIngredient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RecipeSerializerHelper
{
    public static void ingredientMultimapFromJson(JsonObject json, Multimap<Class<?>, RecipeIngredient<?>> multimap)
    {
        json.entrySet().forEach(entry ->
        {
            Class<?> typeClass = IngredientManager.getIngredientHandlerClass(new ResourceLocation(entry.getKey()));

            JsonArray ingredients = entry.getValue().getAsJsonArray();

            ingredients.forEach(element ->
            {
                JsonObject jsonObject = element.getAsJsonObject();
                ResourceLocation identifier = new ResourceLocation(GsonHelper.getAsString(jsonObject, "identifier"));

                multimap.put(typeClass, IngredientManager.getIngredientInstanceFromJSON(identifier, jsonObject));
            });
        });
    }

    public static JsonObject ingredientMultimapToJson(Multimap<Class<?>, RecipeIngredient<?>> multimap)
    {
        var jsonObject = new JsonObject();
        for (var entry : multimap.asMap().entrySet())
        {
            var ingredientArray = new JsonArray();

            for (var ingredient : entry.getValue())
            {
                var ingredientObject = IngredientManager.getIngredientInstanceToJSON(ingredient.getIdentifier(), ingredient);
                ingredientArray.add(ingredientObject);
            }

            jsonObject.add(IngredientManager.getIngredientHandlerIdentifier(entry.getKey()).toString(), ingredientArray);
        }

        return jsonObject;
    }

    public static void ingredientMultimapFromByteBuf(FriendlyByteBuf buffer, Multimap<Class<?>, RecipeIngredient<?>> multimap)
    {
        int typeCount = buffer.readInt();

        List<ResourceLocation> types = new ArrayList<>();

        for (int i = 0; i < typeCount; i++)
            types.add(buffer.readResourceLocation());

        types.forEach(type ->
        {
            Class<?> typeClass = IngredientManager.getIngredientHandlerClass(type);

            int ingredientCount = buffer.readInt();

            for (int i = 0; i < ingredientCount; i++)
                multimap.put(typeClass, IngredientManager.getIngredientInstanceFromBuffer(buffer.readResourceLocation(), buffer));
        });
    }

    public static void ingredientMultimapToByteBuf(FriendlyByteBuf buffer, Multimap<Class<?>, RecipeIngredient<?>> multimap)
    {
        Set<Class<?>> types = multimap.keySet();

        buffer.writeInt(types.size());

        for (Class<?> type : types)
            buffer.writeResourceLocation(IngredientManager.getIngredientHandlerIdentifier(type));

        for (Class<?> type : types)
        {
            Collection<RecipeIngredient<?>> ingredients = multimap.get(type);
            buffer.writeInt(ingredients.size());

            ingredients.forEach(ingredient ->
            {
                buffer.writeResourceLocation(ingredient.getIdentifier());
                ingredient.toBuffer(buffer);
            });
        }
    }
}

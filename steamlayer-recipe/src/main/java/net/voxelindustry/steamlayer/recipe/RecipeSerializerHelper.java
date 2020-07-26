package net.voxelindustry.steamlayer.recipe;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
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
            Class<?> typeClass = IngredientManager.getIngredientHandlerClass(new Identifier(entry.getKey()));

            JsonArray ingredients = entry.getValue().getAsJsonArray();

            ingredients.forEach(element ->
            {
                JsonObject jsonObject = element.getAsJsonObject();
                Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "identifier"));

                multimap.put(typeClass, IngredientManager.getIngredientInstanceFromJSON(identifier, jsonObject));
            });
        });
    }

    public static void ingredientMultimapFromByteBuf(PacketByteBuf buffer, Multimap<Class<?>, RecipeIngredient<?>> multimap)
    {
        int typeCount = buffer.readInt();

        List<Identifier> types = new ArrayList<>();

        for (int i = 0; i < typeCount; i++)
            types.add(buffer.readIdentifier());

        types.forEach(type ->
        {
            Class<?> typeClass = IngredientManager.getIngredientHandlerClass(type);

            int ingredientCount = buffer.readInt();

            for (int i = 0; i < ingredientCount; i++)
                multimap.put(typeClass, IngredientManager.getIngredientInstanceFromBuffer(buffer.readIdentifier(), buffer));
        });
    }

    public static void ingredientMultimapToByteBuf(PacketByteBuf buffer, Multimap<Class<?>, RecipeIngredient<?>> multimap)
    {
        Set<Class<?>> types = multimap.keySet();

        buffer.writeInt(types.size());

        for (Class<?> type : types)
            buffer.writeIdentifier(IngredientManager.getIngredientHandlerIdentifier(type));

        for (Class<?> type : types)
        {
            Collection<RecipeIngredient<?>> ingredients = multimap.get(type);
            buffer.writeInt(ingredients.size());

            ingredients.forEach(ingredient ->
            {
                buffer.writeIdentifier(ingredient.getIdentifier());
                ingredient.toBuffer(buffer);
            });
        }
    }
}

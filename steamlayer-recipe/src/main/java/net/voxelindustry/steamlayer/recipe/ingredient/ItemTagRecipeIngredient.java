package net.voxelindustry.steamlayer.recipe.ingredient;

import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;

import java.util.List;

/**
 * Matches any item of a tag, whatever its components.
 * <p>
 * Tags are bound after recipes are parsed, so the matching items are only looked up when asked for.
 */
public class ItemTagRecipeIngredient extends RecipeIngredient<ItemStack>
{
    public static final Identifier ITEMTAG_INGREDIENT = Identifier.fromNamespaceAndPath(SteamLayerConstants.MODID, "itemtag_ingredient");

    private final TagKey<Item> tag;
    private final int          quantity;

    public ItemTagRecipeIngredient(TagKey<Item> tag, int quantity)
    {
        this.tag = tag;
        this.quantity = quantity;
    }

    public ItemTagRecipeIngredient(TagKey<Item> tag)
    {
        this(tag, 1);
    }

    public TagKey<Item> getTag()
    {
        return tag;
    }

    /**
     * Every item of the tag, one stack each, in tag order. Empty while the tag is unbound.
     */
    public List<ItemStack> getMatchingStacks()
    {
        return BuiltInRegistries.ITEM.get(tag)
                .map(set -> set.stream().map(Holder::value).map(item -> new ItemStack(item, quantity)).toList())
                .orElse(List.of());
    }

    @Override
    public boolean match(ItemStack against)
    {
        return !against.isEmpty() && against.is(tag);
    }

    @Override
    public boolean matchWithQuantity(ItemStack against)
    {
        return quantity <= against.getCount() && match(against);
    }

    @Override
    public int getQuantity()
    {
        return quantity;
    }

    /**
     * The first item of the tag, standing for all of them in recipe states and displays.
     */
    @Override
    public ItemStack getRaw()
    {
        var stacks = getMatchingStacks();
        return stacks.isEmpty() ? ItemStack.EMPTY : stacks.getFirst();
    }

    @Override
    public Identifier getIdentifier()
    {
        return ITEMTAG_INGREDIENT;
    }

    @Override
    public void toBuffer(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeIdentifier(tag.location());
        buffer.writeInt(quantity);
    }

    public static ItemTagRecipeIngredient fromBuffer(RegistryFriendlyByteBuf buffer)
    {
        return new ItemTagRecipeIngredient(TagKey.create(Registries.ITEM, buffer.readIdentifier()), buffer.readInt());
    }

    public static ItemTagRecipeIngredient fromJson(JsonObject json)
    {
        var tag = TagKey.create(Registries.ITEM, Identifier.parse(GsonHelper.getAsString(json, "tag")));
        return new ItemTagRecipeIngredient(tag, GsonHelper.getAsInt(json, "quantity", 1));
    }

    public static JsonObject toJson(RecipeIngredient<?> ingredient)
    {
        var tagIngredient = (ItemTagRecipeIngredient) ingredient;

        var json = new JsonObject();
        json.addProperty("identifier", ingredient.getIdentifier().toString());
        json.addProperty("tag", tagIngredient.tag.location().toString());
        if (tagIngredient.quantity != 1)
            json.addProperty("quantity", tagIngredient.quantity);
        return json;
    }
}

package net.voxelindustry.steamlayer.recipe.ingredient;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;

public class ItemStackRecipeIngredient extends RecipeIngredient<ItemStack>
{
    public static final Identifier ITEMSTACK_INGREDIENT = Identifier.fromNamespaceAndPath(SteamLayerConstants.MODID, "itemstack_ingredient");

    protected final int quantity;
    protected final ItemStack ingredient;

    protected final boolean strictTagComparing;

    public ItemStackRecipeIngredient(ItemStack ingredient, boolean strictTagComparing)
    {
        this.ingredient = ingredient;
        quantity = 0;

        this.strictTagComparing = strictTagComparing;
    }

    public ItemStackRecipeIngredient(ItemStack ingredient)
    {
        this(ingredient, true);
    }

    @Override
    public boolean match(ItemStack against)
    {
        if (strictTagComparing)
            return ItemStack.isSameItemSameComponents(ingredient, against);
        return ItemStack.isSameItem(ingredient, against);
    }

    @Override
    public boolean matchWithQuantity(ItemStack against)
    {
        if (!ingredient.isEmpty())
            return ingredient.getCount() <= against.getCount() && match(against);
        else
            return quantity <= against.getCount();
    }

    @Override
    public int getQuantity()
    {
        if (!ingredient.isEmpty())
            return ingredient.getCount();
        return quantity;
    }

    @Override
    public ItemStack getRaw()
    {
        return ingredient.copy();
    }

    @Override
    public Identifier getIdentifier()
    {
        return ITEMSTACK_INGREDIENT;
    }

    @Override
    public void toBuffer(RegistryFriendlyByteBuf buffer)
    {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, ingredient);
        buffer.writeBoolean(strictTagComparing);
    }

    public static ItemStackRecipeIngredient fromBuffer(RegistryFriendlyByteBuf buffer)
    {
        return new ItemStackRecipeIngredient(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer), buffer.readBoolean());
    }

    public static ItemStackRecipeIngredient fromJson(JsonObject json)
    {
        boolean strictTagComparing = json.has("strictTagComparing") && GsonHelper.getAsBoolean(json, "strictTagComparing");
        return new ItemStackRecipeIngredient(getItemStackFromJson(json), strictTagComparing);
    }

    public static JsonObject toJson(RecipeIngredient<?> ingredient)
    {
        var stackIngredient = ((ItemStackRecipeIngredient) ingredient);

        var json = new JsonObject();

        json.addProperty("identifier", ingredient.getIdentifier().toString());
        if (stackIngredient.strictTagComparing)
            json.addProperty("strictTagComparing", true);

        var stack = stackIngredient.ingredient;

        json.add("item", BuiltInRegistries.ITEM.byNameCodec().encodeStart(JsonOps.INSTANCE, stack.getItem()).getOrThrow());

        if (stack.getCount() > 1)
            json.addProperty("quantity", stack.getCount());

        if (!stack.getComponentsPatch().isEmpty())
            json.add("components", DataComponentPatch.CODEC.encodeStart(JsonOps.INSTANCE, stack.getComponentsPatch()).getOrThrow());

        return json;
    }

    public static ItemStack getItemStackFromJson(JsonObject json)
    {
        Identifier identifier = Identifier.parse(GsonHelper.getAsString(json, "item"));
        Item item = BuiltInRegistries.ITEM.getOptional(identifier).orElseThrow(() ->
                new JsonSyntaxException("Unknown item '" + identifier + "'"));
        int quantity = GsonHelper.isValidPrimitive(json, "quantity") ? GsonHelper.getAsInt(json, "quantity") : 1;

        ItemStack ingredient = new ItemStack(item, quantity);

        if (json.has("components"))
            ingredient.applyComponents(DataComponentPatch.CODEC.parse(JsonOps.INSTANCE, json.get("components"))
                    .getOrThrow(JsonSyntaxException::new));

        return ingredient;
    }
}

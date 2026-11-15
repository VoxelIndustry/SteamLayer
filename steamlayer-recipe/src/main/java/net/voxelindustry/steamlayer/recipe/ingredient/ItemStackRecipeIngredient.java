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
import net.minecraft.world.item.ItemStackTemplate;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;

public class ItemStackRecipeIngredient extends RecipeIngredient<ItemStack>
{
    public static final Identifier ITEMSTACK_INGREDIENT = Identifier.fromNamespaceAndPath(SteamLayerConstants.MODID, "itemstack_ingredient");

    protected final int quantity;
    /**
     * Recipes are parsed before item components are bound, the stack is only created on first use.
     * Null for an empty ingredient.
     */
    protected final ItemStackTemplate template;
    private ItemStack ingredient;

    protected final boolean strictTagComparing;

    public ItemStackRecipeIngredient(ItemStackTemplate template, boolean strictTagComparing)
    {
        this.template = template;
        quantity = 0;

        this.strictTagComparing = strictTagComparing;
    }

    public ItemStackRecipeIngredient(ItemStack ingredient, boolean strictTagComparing)
    {
        this(ingredient.isEmpty() ? null : ItemStackTemplate.fromNonEmptyStack(ingredient), strictTagComparing);
        this.ingredient = ingredient;
    }

    public ItemStackRecipeIngredient(ItemStack ingredient)
    {
        this(ingredient, true);
    }

    protected ItemStack stack()
    {
        if (ingredient == null)
            ingredient = template == null ? ItemStack.EMPTY : template.create();
        return ingredient;
    }

    @Override
    public boolean match(ItemStack against)
    {
        if (strictTagComparing)
            return ItemStack.isSameItemSameComponents(stack(), against);
        return ItemStack.isSameItem(stack(), against);
    }

    @Override
    public boolean matchWithQuantity(ItemStack against)
    {
        if (template != null)
            return template.count() <= against.getCount() && match(against);
        else
            return quantity <= against.getCount();
    }

    @Override
    public int getQuantity()
    {
        if (template != null)
            return template.count();
        return quantity;
    }

    @Override
    public ItemStack getRaw()
    {
        return stack().copy();
    }

    @Override
    public Identifier getIdentifier()
    {
        return ITEMSTACK_INGREDIENT;
    }

    @Override
    public void toBuffer(RegistryFriendlyByteBuf buffer)
    {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack());
        buffer.writeBoolean(strictTagComparing);
    }

    public static ItemStackRecipeIngredient fromBuffer(RegistryFriendlyByteBuf buffer)
    {
        return new ItemStackRecipeIngredient(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer), buffer.readBoolean());
    }

    public static ItemStackRecipeIngredient fromJson(JsonObject json)
    {
        boolean strictTagComparing = json.has("strictTagComparing") && GsonHelper.getAsBoolean(json, "strictTagComparing");
        return new ItemStackRecipeIngredient(getTemplateFromJson(json), strictTagComparing);
    }

    public static JsonObject toJson(RecipeIngredient<?> ingredient)
    {
        var stackIngredient = ((ItemStackRecipeIngredient) ingredient);

        var json = new JsonObject();

        json.addProperty("identifier", ingredient.getIdentifier().toString());
        if (stackIngredient.strictTagComparing)
            json.addProperty("strictTagComparing", true);

        var template = stackIngredient.template;

        json.add("item", BuiltInRegistries.ITEM.byNameCodec().encodeStart(JsonOps.INSTANCE, template.item().value()).getOrThrow());

        if (template.count() > 1)
            json.addProperty("quantity", template.count());

        if (!template.components().isEmpty())
            json.add("components", DataComponentPatch.CODEC.encodeStart(JsonOps.INSTANCE, template.components()).getOrThrow());

        return json;
    }

    public static ItemStackTemplate getTemplateFromJson(JsonObject json)
    {
        Identifier identifier = Identifier.parse(GsonHelper.getAsString(json, "item"));
        Item item = BuiltInRegistries.ITEM.getOptional(identifier).orElseThrow(() ->
                new JsonSyntaxException("Unknown item '" + identifier + "'"));
        int quantity = GsonHelper.isValidPrimitive(json, "quantity") ? GsonHelper.getAsInt(json, "quantity") : 1;

        DataComponentPatch components = DataComponentPatch.EMPTY;
        if (json.has("components"))
            components = DataComponentPatch.CODEC.parse(JsonOps.INSTANCE, json.get("components"))
                    .getOrThrow(JsonSyntaxException::new);

        return new ItemStackTemplate(item.builtInRegistryHolder(), quantity, components);
    }
}

package net.voxelindustry.steamlayer.recipe.ingredient;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;

public class ItemStackRecipeIngredient extends RecipeIngredient<ItemStack>
{
    public static final ResourceLocation ITEMSTACK_INGREDIENT = new ResourceLocation(SteamLayerConstants.MODID, "itemstack_ingredient");

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
            return ItemStack.isSameItemSameTags(ingredient, against);
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
    public ResourceLocation getIdentifier()
    {
        return ITEMSTACK_INGREDIENT;
    }

    @Override
    public void toBuffer(FriendlyByteBuf buffer)
    {
        buffer.writeItem(ingredient);
        buffer.writeBoolean(strictTagComparing);
    }

    public static ItemStackRecipeIngredient fromBuffer(FriendlyByteBuf buffer)
    {
        return new ItemStackRecipeIngredient(buffer.readItem(), buffer.readBoolean());
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

        json.add("item", BuiltInRegistries.ITEM.byNameCodec().encodeStart(JsonOps.INSTANCE, stack.getItem()).get().left().get());

        if (stack.getCount() > 1)
            json.addProperty("quantity", stack.getCount());

        if (stack.hasTag())
            json.add("nbt", CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, stack.getTag()).getOrThrow(false, error ->
            {
                throw new RuntimeException(error);
            }));

        return json;
    }

    public static ItemStack getItemStackFromJson(JsonObject json)
    {
        ResourceLocation identifier = new ResourceLocation(GsonHelper.getAsString(json, "item"));
        Item item = BuiltInRegistries.ITEM.getOptional(identifier).orElseThrow(() ->
                new JsonSyntaxException("Unknown item '" + identifier + "'"));
        int quantity = GsonHelper.isValidPrimitive(json, "quantity") ? GsonHelper.getAsInt(json, "quantity") : 1;

        ItemStack ingredient = new ItemStack(item, quantity);

        if (json.has("nbt"))
        {
            CompoundTag tagCompound = CompoundTag.CODEC.parse(JsonOps.INSTANCE, json.getAsJsonObject("nbt"))
                    .getOrThrow(false, SteamLayerConstants.LOGGER::error);

            ingredient.setTag(tagCompound);
        }

        return ingredient;
    }
}

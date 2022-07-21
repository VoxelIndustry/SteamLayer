package net.voxelindustry.steamlayer.recipe.ingredient;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;
import net.voxelindustry.steamlayer.common.utils.ItemUtils;

public class ItemStackRecipeIngredient extends RecipeIngredient<ItemStack>
{
    public static final Identifier ITEMSTACK_INGREDIENT = new Identifier(SteamLayerConstants.MODID, "itemstack_ingredient");

    protected final int       quantity;
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
            return ItemUtils.deepEquals(ingredient, against);
        return ItemUtils.equalsIgnoringTag(ingredient, against);
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
    public void toBuffer(PacketByteBuf buffer)
    {
        buffer.writeItemStack(ingredient);
        buffer.writeBoolean(strictTagComparing);
    }

    public static ItemStackRecipeIngredient fromBuffer(PacketByteBuf buffer)
    {
        return new ItemStackRecipeIngredient(buffer.readItemStack(), buffer.readBoolean());
    }

    public static ItemStackRecipeIngredient fromJson(JsonObject json)
    {
        boolean strictTagComparing = json.has("strictTagComparing") && JsonHelper.getBoolean(json, "strictTagComparing");
        return new ItemStackRecipeIngredient(getItemStackFromJson(json), strictTagComparing);
    }

    public static ItemStack getItemStackFromJson(JsonObject json)
    {
        Identifier identifier = new Identifier(JsonHelper.getString(json, "item"));
        Item item = Registry.ITEM.getOrEmpty(identifier).orElseThrow(() ->
                new JsonSyntaxException("Unknown item '" + identifier + "'"));
        int quantity = JsonHelper.hasPrimitive(json, "quantity") ? JsonHelper.getInt(json, "quantity") : 1;

        ItemStack ingredient = new ItemStack(item, quantity);

        if (json.has("nbt"))
        {
            NbtCompound tagCompound = NbtCompound.CODEC.parse(JsonOps.INSTANCE, json.getAsJsonObject("nbt"))
                    .getOrThrow(false, SteamLayerConstants.LOGGER::error);

            ingredient.setNbt(tagCompound);
        }

        return ingredient;
    }
}

package net.voxelindustry.steamlayer.recipe.ingredient;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Range;

public class ItemStackRandRecipeIngredient extends ItemStackRecipeIngredient
{
    public static final Identifier ITEMSTACK_RAND_INGREDIENT = new Identifier(SteamLayerConstants.MODID, "itemstack_rand_ingredient");

    private Range<Integer> range;

    public ItemStackRandRecipeIngredient(ItemStack ingredient, Range<Integer> range)
    {
        super(ingredient);

        this.range = range;
    }

    public ItemStackRandRecipeIngredient(ItemStack ingredient, Range<Integer> range, boolean strictTagComparing)
    {
        super(ingredient, strictTagComparing);

        this.range = range;
    }

    @Override
    public ItemStack getRaw()
    {
        return ingredient.copyWithCount(RandomUtils.nextInt(range.getMinimum(), range.getMaximum()));
    }

    @Override
    public RecipeIngredient<ItemStack> staticForState()
    {
        return new ItemStackRecipeIngredient(getRaw());
    }

    @Override
    public Identifier getIdentifier()
    {
        return ITEMSTACK_RAND_INGREDIENT;
    }

    @Override
    public void toBuffer(PacketByteBuf buffer)
    {
        super.toBuffer(buffer);
        buffer.writeInt(range.getMinimum());
        buffer.writeInt(range.getMaximum());
    }

    public static ItemStackRandRecipeIngredient fromBuffer(PacketByteBuf buffer)
    {
        ItemStack stack = buffer.readItemStack();
        boolean strictTagComparing = buffer.readBoolean();
        Range<Integer> range = Range.between(buffer.readInt(), buffer.readInt());

        return new ItemStackRandRecipeIngredient(stack, range, strictTagComparing);
    }

    public static ItemStackRandRecipeIngredient fromJson(JsonObject json)
    {
        boolean strictTagComparing = json.has("strictTagComparing") && JsonHelper.getBoolean(json, "strictTagComparing");
        Range<Integer> range = Range.between(JsonHelper.getInt(json, "min"), JsonHelper.getInt(json, "max"));

        return new ItemStackRandRecipeIngredient(getItemStackFromJson(json), range, strictTagComparing);
    }

    public static JsonObject toJson(RecipeIngredient<?> ingredient)
    {
        var stackIngredient = ((ItemStackRandRecipeIngredient) ingredient);

        var json = new JsonObject();

        if (stackIngredient.strictTagComparing)
            json.addProperty("strictTagComparing", true);

        var stack = stackIngredient.ingredient;

        json.addProperty("item", stack.getItem().toString());

        if (stack.getCount() > 1)
            json.addProperty("quantity", stack.getCount());

        json.addProperty("min", stackIngredient.range.getMinimum());
        json.addProperty("max", stackIngredient.range.getMaximum());

        if (stack.hasNbt())
            json.add("nbt", NbtCompound.CODEC.encodeStart(JsonOps.INSTANCE, stack.getNbt()).getOrThrow(false, error ->
            {
                throw new RuntimeException(error);
            }));

        return json;
    }
}

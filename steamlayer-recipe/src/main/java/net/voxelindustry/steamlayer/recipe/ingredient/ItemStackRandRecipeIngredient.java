package net.voxelindustry.steamlayer.recipe.ingredient;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Range;

public class ItemStackRandRecipeIngredient extends ItemStackRecipeIngredient
{
    public static final ResourceLocation ITEMSTACK_RAND_INGREDIENT = new ResourceLocation(SteamLayerConstants.MODID, "itemstack_rand_ingredient");

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
    public ResourceLocation getIdentifier()
    {
        return ITEMSTACK_RAND_INGREDIENT;
    }

    @Override
    public void toBuffer(FriendlyByteBuf buffer)
    {
        super.toBuffer(buffer);
        buffer.writeInt(range.getMinimum());
        buffer.writeInt(range.getMaximum());
    }

    public static ItemStackRandRecipeIngredient fromBuffer(FriendlyByteBuf buffer)
    {
        ItemStack stack = buffer.readItem();
        boolean strictTagComparing = buffer.readBoolean();
        Range<Integer> range = Range.between(buffer.readInt(), buffer.readInt());

        return new ItemStackRandRecipeIngredient(stack, range, strictTagComparing);
    }

    public static ItemStackRandRecipeIngredient fromJson(JsonObject json)
    {
        boolean strictTagComparing = json.has("strictTagComparing") && GsonHelper.getAsBoolean(json, "strictTagComparing");
        Range<Integer> range = Range.between(GsonHelper.getAsInt(json, "min"), GsonHelper.getAsInt(json, "max"));

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

        if (stack.hasTag())
            json.add("nbt", CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, stack.getTag()).getOrThrow(false, error ->
            {
                throw new RuntimeException(error);
            }));

        return json;
    }
}

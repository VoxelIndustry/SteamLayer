package net.voxelindustry.steamlayer.recipe.ingredient;

import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Range;

public class ItemStackRandRecipeIngredient extends ItemStackRecipeIngredient
{
    public static final Identifier ITEMSTACK_RAND_INGREDIENT = Identifier.fromNamespaceAndPath(SteamLayerConstants.MODID, "itemstack_rand_ingredient");

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
    public void toBuffer(RegistryFriendlyByteBuf buffer)
    {
        super.toBuffer(buffer);
        buffer.writeInt(range.getMinimum());
        buffer.writeInt(range.getMaximum());
    }

    public static ItemStackRandRecipeIngredient fromBuffer(RegistryFriendlyByteBuf buffer)
    {
        ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
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
        var json = ItemStackRecipeIngredient.toJson(ingredient);
        var range = ((ItemStackRandRecipeIngredient) ingredient).range;

        json.addProperty("min", range.getMinimum());
        json.addProperty("max", range.getMaximum());
        return json;
    }
}

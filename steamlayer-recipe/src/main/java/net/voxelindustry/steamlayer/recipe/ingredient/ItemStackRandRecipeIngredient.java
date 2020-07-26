package net.voxelindustry.steamlayer.recipe.ingredient;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;
import net.voxelindustry.steamlayer.common.utils.ItemUtils;
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
        return ItemUtils.copyWithSize(ingredient, RandomUtils.nextInt(range.getMinimum(), range.getMaximum()));
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
}

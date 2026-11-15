package net.voxelindustry.steamlayer.recipe.ingredient;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.voxelindustry.steamlayer.common.SteamLayerConstants;
import org.jspecify.annotations.Nullable;

/**
 * Output that copies an input stack, components and count included, optionally turns it into another item, then applies a component patch to it.
 * <p>
 * It gives an enchanted sword back enchanted with one more component, or raises an item to its next tier keeping what was set in it.
 */
public class ItemStackTransformIngredient extends RecipeIngredient<ItemStack> implements TransformingIngredient<ItemStack>
{
    public static final Identifier ITEMSTACK_TRANSFORM_INGREDIENT = Identifier.fromNamespaceAndPath(SteamLayerConstants.MODID, "itemstack_transform_ingredient");

    private final int                sourceSlot;
    @Nullable
    private final Item               item;
    private final DataComponentPatch patch;

    /**
     * @param sourceSlot index of the input stack to transform
     * @param item       the item the copy becomes, or null to keep the input item
     * @param patch      components set on, or removed from, the copy
     */
    public ItemStackTransformIngredient(int sourceSlot, @Nullable Item item, DataComponentPatch patch)
    {
        this.sourceSlot = sourceSlot;
        this.item = item;
        this.patch = patch;
    }

    public static ItemStackTransformIngredient patching(DataComponentPatch patch)
    {
        return new ItemStackTransformIngredient(0, null, patch);
    }

    public static ItemStackTransformIngredient transmuting(Item item)
    {
        return new ItemStackTransformIngredient(0, item, DataComponentPatch.EMPTY);
    }

    @Override
    public int getSourceSlot()
    {
        return sourceSlot;
    }

    @Nullable
    public Item getItem()
    {
        return item;
    }

    public DataComponentPatch getPatch()
    {
        return patch;
    }

    @Override
    public ItemStack transform(ItemStack source)
    {
        if (source.isEmpty())
            return ItemStack.EMPTY;

        var result = item == null ? source.copy() : source.transmuteCopy(item, source.getCount());
        result.applyComponents(patch);
        return result;
    }

    /**
     * Whether a stack could have come out of this output: the target item, if there is one, already carrying the patch.
     */
    @Override
    public boolean match(ItemStack against)
    {
        if (against.isEmpty() || item != null && !against.is(item))
            return false;
        return ItemStack.isSameItemSameComponents(transform(against), against);
    }

    @Override
    public boolean matchWithQuantity(ItemStack against)
    {
        return match(against);
    }

    @Override
    public int getQuantity()
    {
        return 1;
    }

    /**
     * Without an input to work from, only the target item with the patch applied, or empty when the input item is kept.
     */
    @Override
    public ItemStack getRaw()
    {
        if (item == null)
            return ItemStack.EMPTY;
        return transform(new ItemStack(item));
    }

    @Override
    public Identifier getIdentifier()
    {
        return ITEMSTACK_TRANSFORM_INGREDIENT;
    }

    @Override
    public void toBuffer(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeVarInt(sourceSlot);
        buffer.writeBoolean(item != null);
        if (item != null)
            buffer.writeIdentifier(BuiltInRegistries.ITEM.getKey(item));
        DataComponentPatch.STREAM_CODEC.encode(buffer, patch);
    }

    public static ItemStackTransformIngredient fromBuffer(RegistryFriendlyByteBuf buffer)
    {
        int sourceSlot = buffer.readVarInt();
        Item item = buffer.readBoolean() ? BuiltInRegistries.ITEM.getValue(buffer.readIdentifier()) : null;
        return new ItemStackTransformIngredient(sourceSlot, item, DataComponentPatch.STREAM_CODEC.decode(buffer));
    }

    public static ItemStackTransformIngredient fromJson(JsonObject json)
    {
        int sourceSlot = GsonHelper.getAsInt(json, "source", 0);

        Item item = null;
        if (json.has("item"))
        {
            var identifier = Identifier.parse(GsonHelper.getAsString(json, "item"));
            item = BuiltInRegistries.ITEM.getOptional(identifier).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + identifier + "'"));
        }

        var patch = DataComponentPatch.EMPTY;
        if (json.has("components"))
            patch = DataComponentPatch.CODEC.parse(JsonOps.INSTANCE, json.get("components")).getOrThrow(JsonSyntaxException::new);

        return new ItemStackTransformIngredient(sourceSlot, item, patch);
    }

    public static JsonObject toJson(RecipeIngredient<?> ingredient)
    {
        var transform = (ItemStackTransformIngredient) ingredient;

        var json = new JsonObject();
        json.addProperty("identifier", ingredient.getIdentifier().toString());
        if (transform.sourceSlot != 0)
            json.addProperty("source", transform.sourceSlot);
        if (transform.item != null)
            json.addProperty("item", BuiltInRegistries.ITEM.getKey(transform.item).toString());
        if (!transform.patch.isEmpty())
            json.add("components", DataComponentPatch.CODEC.encodeStart(JsonOps.INSTANCE, transform.patch).getOrThrow());
        return json;
    }
}

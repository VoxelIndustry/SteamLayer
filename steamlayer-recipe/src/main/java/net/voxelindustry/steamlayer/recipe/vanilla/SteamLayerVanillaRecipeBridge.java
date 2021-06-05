package net.voxelindustry.steamlayer.recipe.vanilla;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.world.World;

public interface SteamLayerVanillaRecipeBridge extends Recipe<Inventory>
{
    @Deprecated
    @Override
    default boolean matches(Inventory inv, World worldIn)
    {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default ItemStack craft(Inventory inv)
    {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default boolean fits(int width, int height)
    {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default ItemStack getOutput()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isIgnoredInRecipeBook()
    {
        return true;
    }
}

package net.voxelindustry.steamlayer.recipe.vanilla;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

public interface SteamLayerVanillaRecipeBridge extends Recipe<Container>
{
    @Deprecated
    @Override
    default boolean matches(Container inv, Level worldIn)
    {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    default boolean canCraftInDimensions(int width, int height)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isSpecial()
    {
        return true;
    }
}

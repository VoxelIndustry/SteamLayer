package net.voxelindustry.steamlayer.recipe.ingredient;

import net.minecraft.fluid.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FluidStackRecipeIngredient extends RecipeIngredient<FluidStack>
{
    private FluidStack ingredient;

    public FluidStackRecipeIngredient(FluidStack ingredient)
    {
        this.ingredient = ingredient;
    }

    public FluidStackRecipeIngredient(Fluid ingredient)
    {
        this(new FluidStack(ingredient, 1));
    }

    @Override
    public boolean match(FluidStack against)
    {
        return against.getFluid().equals(getRaw().getFluid())
                && FluidStack.areFluidStackTagsEqual(against, getRaw());
    }

    @Override
    public boolean matchWithQuantity(FluidStack against)
    {
        return match(against) && getQuantity() <= against.getAmount();
    }

    @Override
    public int getQuantity()
    {
        return ingredient.getAmount();
    }

    @Override
    public FluidStack getRaw()
    {
        return ingredient;
    }
}

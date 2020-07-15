package net.voxelindustry.steamlayer.recipe.category;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import net.minecraft.item.ItemStack;
import net.voxelindustry.steamlayer.recipe.RecipeBase;
import net.voxelindustry.steamlayer.recipe.ingredient.ItemStackRecipeIngredient;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public class DummyItemStackRecipe extends RecipeBase
{
    @Singular
    private final List<ItemStack> inputStacks;
    @Singular
    private final List<ItemStack> outputStacks;

    @Getter
    private final int time;

    public DummyItemStackRecipe(List<ItemStack> inputs, List<ItemStack> outputs, int time)
    {
        inputStacks = inputs;
        outputStacks = outputs;
        this.time = time;

        this.inputs.putAll(ItemStack.class, inputStacks.stream().map(ItemStackRecipeIngredient::new).collect(Collectors.toList()));
        this.outputs.putAll(ItemStack.class, outputStacks.stream().map(ItemStackRecipeIngredient::new).collect(Collectors.toList()));
    }
}

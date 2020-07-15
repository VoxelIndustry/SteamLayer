package net.voxelindustry.steamlayer.recipe.category;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.voxelindustry.steamlayer.common.utils.ItemUtils;
import net.voxelindustry.steamlayer.recipe.state.RecipeState;
import net.voxelindustry.steamlayer.test.SteamLayerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SteamLayerExtension.class)
public class RecipeStateTest
{
    @Test
    void consumeSlotted_givenOneFulfilledInput_thenShouldKeepOneAsLeftOver()
    {
        DummyItemStackRecipe recipe = DummyItemStackRecipe.builder()
                .inputStack(new ItemStack(Items.APPLE))
                .inputStack(new ItemStack(Items.POTATO))
                .outputStack(new ItemStack(Items.CARROT))
                .time(20)
                .build();

        RecipeState state = recipe.createState();

        state.consumeSlotted(ItemStack.class, new ItemStack(Items.APPLE), 0);

        assertThat(state.getIngredientsLeft(ItemStack.class).get(0).isEmpty()).isTrue();
        assertThat(ItemUtils.deepEquals(state.getIngredientsLeft(ItemStack.class).get(1), new ItemStack(Items.POTATO))).isTrue();
    }

    @Test
    void consumeSlotted_givenOnePartiallyFulfilledInput_thenShouldKeepPartiallyConsumedAsLeftOver()
    {
        DummyItemStackRecipe recipe = DummyItemStackRecipe.builder()
                .inputStack(new ItemStack(Items.APPLE, 3))
                .inputStack(new ItemStack(Items.POTATO))
                .outputStack(new ItemStack(Items.CARROT))
                .time(20)
                .build();

        RecipeState state = recipe.createState();

        state.consumeSlotted(ItemStack.class, new ItemStack(Items.APPLE), 0);

        assertThat(ItemUtils.deepEquals(state.getIngredientsLeft(ItemStack.class).get(0), new ItemStack(Items.APPLE, 2))).isTrue();
        assertThat(ItemUtils.deepEquals(state.getIngredientsLeft(ItemStack.class).get(1), new ItemStack(Items.POTATO))).isTrue();
    }

    @Test
    void consumeSlotless_givenOnePartiallyFulfilledInput_thenShouldKeepPartiallyConsumedAsLeftOver()
    {
        DummyItemStackRecipe recipe = DummyItemStackRecipe.builder()
                .inputStack(new ItemStack(Items.APPLE, 3))
                .inputStack(new ItemStack(Items.POTATO))
                .outputStack(new ItemStack(Items.CARROT))
                .time(20)
                .build();

        RecipeState state = recipe.createState();

        state.consumeSlotless(ItemStack.class, new ItemStack(Items.POTATO));

        assertThat(ItemUtils.deepEquals(state.getIngredientsLeft(ItemStack.class).get(0), new ItemStack(Items.APPLE, 3))).isTrue();
        assertThat(state.getIngredientsLeft(ItemStack.class).get(1).isEmpty()).isTrue();
    }

    @Test
    void isCompleted_givenAllFulfilledSlottedAndTime_thenShouldMarkAsCompleted()
    {
        DummyItemStackRecipe recipe = DummyItemStackRecipe.builder()
                .inputStack(new ItemStack(Items.APPLE, 3))
                .inputStack(new ItemStack(Items.POTATO))
                .outputStack(new ItemStack(Items.CARROT))
                .time(20)
                .build();

        RecipeState state = recipe.createState();

        state.consumeSlotted(ItemStack.class, new ItemStack(Items.APPLE, 3), 0);
        state.consumeSlotted(ItemStack.class, new ItemStack(Items.POTATO), 1);
        state.tick(20);

        assertThat(state.getIngredientsLeft(ItemStack.class).get(0).isEmpty()).isTrue();
        assertThat(state.getIngredientsLeft(ItemStack.class).get(1).isEmpty()).isTrue();

        assertThat(state.isCompleted()).isTrue();
    }

    @Test
    void isCompleted_givenAllFulfilledSlotlessAndTime_thenShouldMarkAsCompleted()
    {
        DummyItemStackRecipe recipe = DummyItemStackRecipe.builder()
                .inputStack(new ItemStack(Items.APPLE, 3))
                .inputStack(new ItemStack(Items.POTATO))
                .outputStack(new ItemStack(Items.CARROT))
                .time(20)
                .build();

        RecipeState state = recipe.createState();

        state.consumeSlotless(ItemStack.class, new ItemStack(Items.POTATO));
        state.consumeSlotless(ItemStack.class, new ItemStack(Items.APPLE));
        state.consumeSlotless(ItemStack.class, new ItemStack(Items.APPLE));
        state.consumeSlotless(ItemStack.class, new ItemStack(Items.APPLE));
        state.tick(20);

        assertThat(state.getIngredientsLeft(ItemStack.class).get(0).isEmpty()).isTrue();
        assertThat(state.getIngredientsLeft(ItemStack.class).get(1).isEmpty()).isTrue();

        assertThat(state.isCompleted()).isTrue();
    }
}

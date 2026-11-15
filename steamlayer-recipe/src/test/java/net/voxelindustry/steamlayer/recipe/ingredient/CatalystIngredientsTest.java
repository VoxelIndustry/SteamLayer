package net.voxelindustry.steamlayer.recipe.ingredient;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.voxelindustry.steamlayer.recipe.IngredientManager;
import net.voxelindustry.steamlayer.test.SteamLayerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SteamLayerExtension.class)
public class CatalystIngredientsTest
{
    private static ItemStack namedSword()
    {
        var sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("Old Friend"));
        sword.setDamageValue(42);
        return sword;
    }

    @Test
    void transform_givenPatch_thenKeepsInputComponentsAndAddsPatch()
    {
        var transform = ItemStackTransformIngredient.patching(DataComponentPatch.builder().set(DataComponents.RARITY, Rarity.EPIC).build());
        var sword = namedSword();

        var result = transform.transform(sword);

        assertThat(result.is(Items.IRON_SWORD)).isTrue();
        assertThat(result.get(DataComponents.CUSTOM_NAME)).isEqualTo(Component.literal("Old Friend"));
        assertThat(result.getDamageValue()).isEqualTo(42);
        assertThat(result.get(DataComponents.RARITY)).isEqualTo(Rarity.EPIC);
        assertThat(sword.get(DataComponents.RARITY)).as("the input is left untouched").isNotEqualTo(Rarity.EPIC);
    }

    @Test
    void transform_givenRemovalPatch_thenRemovesComponent()
    {
        var sword = namedSword();
        var transform = ItemStackTransformIngredient.patching(DataComponentPatch.builder().remove(DataComponents.CUSTOM_NAME).build());

        assertThat(transform.transform(sword).has(DataComponents.CUSTOM_NAME)).isFalse();
    }

    @Test
    void transform_givenItem_thenTransmutesKeepingComponents()
    {
        var transform = ItemStackTransformIngredient.transmuting(Items.DIAMOND_SWORD);

        var result = transform.transform(namedSword());

        assertThat(result.is(Items.DIAMOND_SWORD)).isTrue();
        assertThat(result.get(DataComponents.CUSTOM_NAME)).isEqualTo(Component.literal("Old Friend"));
    }

    @Test
    void match_givenStackAlreadyCarryingPatch_thenMatches()
    {
        var transform = ItemStackTransformIngredient.patching(DataComponentPatch.builder().set(DataComponents.RARITY, Rarity.EPIC).build());
        var sword = namedSword();

        assertThat(transform.match(sword)).isFalse();
        assertThat(transform.match(transform.transform(sword))).isTrue();
    }

    @Test
    void transformJson_givenRoundTrip_thenEqualBehaviour()
    {
        var transform = new ItemStackTransformIngredient(1, Items.GOLDEN_SWORD, DataComponentPatch.builder()
                .set(DataComponents.RARITY, Rarity.RARE)
                .remove(DataComponents.CUSTOM_NAME)
                .build());

        var json = IngredientManager.getIngredientInstanceToJSON(transform.getIdentifier(), transform);
        var read = (ItemStackTransformIngredient) IngredientManager.getIngredientInstanceFromJSON(transform.getIdentifier(), json);

        assertThat(read.getSourceSlot()).isEqualTo(1);
        assertThat(read.getItem()).isEqualTo(Items.GOLDEN_SWORD);
        assertThat(read.getPatch()).isEqualTo(transform.getPatch());
    }

    @Test
    void tagJson_givenRoundTrip_thenSameTagAndQuantity()
    {
        var ingredient = new ItemTagRecipeIngredient(ItemTags.SWORDS, 2);

        var json = IngredientManager.getIngredientInstanceToJSON(ingredient.getIdentifier(), ingredient);
        var read = (ItemTagRecipeIngredient) IngredientManager.getIngredientInstanceFromJSON(ingredient.getIdentifier(), json);

        assertThat(read.getTag()).isEqualTo(ItemTags.SWORDS);
        assertThat(read.getQuantity()).isEqualTo(2);
    }

    @Test
    void tagMatch_givenEmptyStack_thenNoMatch()
    {
        var ingredient = new ItemTagRecipeIngredient(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("steamlayer", "anything")));

        assertThat(ingredient.match(ItemStack.EMPTY)).isFalse();
    }
}

package net.voxelindustry.steamlayer.recipe.ingredient;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

public interface IngredientHandler<T>
{
    T accumulateMutator(T first, T second);

    T accumulateCreate(T first, T second);

    T subtractMutator(T first, T second);

    T subtractCreate(T first, T second);

    boolean canMerge(T first, T second);

    boolean isEmpty(T value);

    CompoundTag toTag(T value, CompoundTag tag);

    T fromTag(CompoundTag tag);

    Identifier getIdentifier();
}

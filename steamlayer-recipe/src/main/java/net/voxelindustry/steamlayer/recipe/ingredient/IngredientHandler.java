package net.voxelindustry.steamlayer.recipe.ingredient;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public interface IngredientHandler<T>
{
    T accumulateMutator(T first, T second);

    T accumulateCreate(T first, T second);

    T subtractMutator(T first, T second);

    T subtractCreate(T first, T second);

    boolean canMerge(T first, T second);

    boolean isEmpty(T value);

    T getEmpty();

    NbtCompound toTag(T value, NbtCompound tag);

    T fromTag(NbtCompound tag);

    Identifier getIdentifier();
}

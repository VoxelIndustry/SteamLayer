package net.voxelindustry.steamlayer.recipe.ingredient;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.Identifier;

public interface IngredientHandler<T>
{
    T accumulateMutator(T first, T second);

    T accumulateCreate(T first, T second);

    T subtractMutator(T first, T second);

    T subtractCreate(T first, T second);

    boolean canMerge(T first, T second);

    boolean isEmpty(T value);

    T getEmpty();

    void save(T value, ValueOutput output);

    T load(ValueInput input);

    Identifier getIdentifier();
}

package net.voxelindustry.steamlayer.recipe.ingredient;

/**
 * An output made from one of the recipe inputs rather than from nothing, so the data it carries is kept.
 *
 * @param <T> the ingredient type
 */
public interface TransformingIngredient<T>
{
    /**
     * @return the index, among the inputs of the same type, of the input this output is made from
     */
    int getSourceSlot();

    /**
     * @param source the actual input in the source slot, left untouched
     * @return the output made from it
     */
    T transform(T source);
}

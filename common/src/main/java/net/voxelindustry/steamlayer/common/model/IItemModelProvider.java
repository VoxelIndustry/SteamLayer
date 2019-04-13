package net.voxelindustry.steamlayer.common.model;

public interface IItemModelProvider
{
    default void registerModels()
    {

    }

    default void registerVariants()
    {

    }

    default boolean hasSpecialModel()
    {
        return false;
    }
}

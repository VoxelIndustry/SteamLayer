package net.voxelindustry.steamlayer.recipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.voxelindustry.steamlayer.recipe.state.RecipeState;

@FunctionalInterface
public interface RecipeCallback
{
    void run(Level world, BlockPos pos, RecipeState state, LivingEntity crafter);
}

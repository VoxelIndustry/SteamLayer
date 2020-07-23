package net.voxelindustry.steamlayer.recipe;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.voxelindustry.steamlayer.recipe.state.RecipeState;

@FunctionalInterface
public interface RecipeCallback
{
    void run(World world, BlockPos pos, RecipeState state, LivingEntity crafter);
}

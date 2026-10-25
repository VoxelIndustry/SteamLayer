package net.voxelindustry.steamlayer.recipe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

@Mixin(RecipeManager.class)
public interface AccessorRecipeManager
{
    @Invoker(value = "byType")
    <C extends Container, T extends Recipe<C>> Map<ResourceLocation, Recipe<C>> getAll(RecipeType<T> type);
}
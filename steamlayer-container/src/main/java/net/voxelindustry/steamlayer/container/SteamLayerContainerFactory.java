package net.voxelindustry.steamlayer.container;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;

/**
 * Opens a {@link BuiltContainer} on the client from the position of the tile providing it.
 * Pair the tile with an {@link net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider} returning its {@link BlockPos}.
 */
public class SteamLayerContainerFactory<T extends BuiltContainer> implements ExtendedMenuType.ExtendedFactory<T, BlockPos>
{
    public static <T extends BuiltContainer> ExtendedMenuType<T, BlockPos> createMenuType()
    {
        return new ExtendedMenuType<>(new SteamLayerContainerFactory<>(), BlockPos.STREAM_CODEC);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T create(int syncID, Inventory playerInventory, BlockPos pos)
    {
        return (T) ((MenuProvider) playerInventory.player.level().getBlockEntity(pos)).createMenu(syncID, playerInventory, playerInventory.player);
    }
}

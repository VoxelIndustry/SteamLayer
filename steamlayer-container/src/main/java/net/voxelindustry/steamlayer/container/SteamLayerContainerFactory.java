package net.voxelindustry.steamlayer.container;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry.ExtendedClientHandlerFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;

public class SteamLayerContainerFactory<T extends BuiltContainer> implements ExtendedClientHandlerFactory<T>
{
    @Override
    public T create(int syncID, Inventory playerInventory, FriendlyByteBuf extraData)
    {
        return (T) ((MenuProvider) Minecraft.getInstance().level.getBlockEntity(extraData.readBlockPos())).createMenu(syncID, playerInventory, playerInventory.player);
    }
}

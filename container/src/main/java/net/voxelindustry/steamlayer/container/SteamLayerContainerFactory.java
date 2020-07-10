package net.voxelindustry.steamlayer.container;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry.ExtendedClientHandlerFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;

public class SteamLayerContainerFactory<T extends BuiltContainer> implements ExtendedClientHandlerFactory<T>
{
    @Override
    public T create(int syncID, PlayerInventory playerInventory, PacketByteBuf extraData)
    {
        return (T) ((NamedScreenHandlerFactory) MinecraftClient.getInstance().world.getBlockEntity(extraData.readBlockPos())).createMenu(syncID, playerInventory, playerInventory.player);
    }
}

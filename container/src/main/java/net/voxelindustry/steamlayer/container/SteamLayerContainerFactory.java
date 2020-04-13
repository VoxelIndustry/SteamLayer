package net.voxelindustry.steamlayer.container;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.IContainerFactory;

public class SteamLayerContainerFactory<T extends BuiltContainer> implements IContainerFactory<T>
{
    public static <T extends BuiltContainer> ContainerType<T> create()
    {
        return new ContainerType<>(new SteamLayerContainerFactory<>());
    }

    @Override
    public T create(int windowId, PlayerInventory playerInv, PacketBuffer extraData)
    {
        return (T) ((INamedContainerProvider) Minecraft.getInstance().world.getTileEntity(extraData.readBlockPos())).createMenu(windowId, playerInv, playerInv.player);
    }
}

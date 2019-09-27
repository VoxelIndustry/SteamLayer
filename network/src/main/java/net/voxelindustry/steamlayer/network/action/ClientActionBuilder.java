package net.voxelindustry.steamlayer.network.action;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.PacketDistributor;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;
import net.voxelindustry.steamlayer.network.packet.ClientActionHolderPacket;

public class ClientActionBuilder
{
    private TileEntity   sender;
    private PlayerEntity player;
    private CompoundNBT  payload;
    private int          replyID;

    public ClientActionBuilder(int replyID, TileEntity sender)
    {
        this.sender = sender;
        payload = new CompoundNBT();
        this.replyID = replyID;
    }

    public ClientActionBuilder toPlayer(PlayerEntity player)
    {
        this.player = player;
        return this;
    }

    public ClientActionBuilder withInt(String key, Integer value)
    {
        payload.putInt(key, value);
        return this;
    }

    public ClientActionBuilder withLong(String key, Long value)
    {
        payload.putLong(key, value);
        return this;
    }

    public ClientActionBuilder withString(String key, String value)
    {
        payload.putString(key, value);
        return this;
    }

    public ClientActionBuilder withFloat(String key, Float value)
    {
        payload.putFloat(key, value);
        return this;
    }

    public ClientActionBuilder withDouble(String key, Double value)
    {
        payload.putDouble(key, value);
        return this;
    }

    public ClientActionBuilder withBoolean(String key, Boolean value)
    {
        payload.putBoolean(key, value);
        return this;
    }

    public ClientActionBuilder withItemStack(String key, ItemStack value)
    {
        payload.put(key, value.write(new CompoundNBT()));
        return this;
    }

    public ClientActionBuilder withFluidStack(String key, FluidStack value)
    {
        payload.put(key, value.writeToNBT(new CompoundNBT()));
        return this;
    }

    public void send()
    {
        SteamLayerPacketHandler.getHandler().send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new ClientActionHolderPacket(replyID, payload));
    }
}

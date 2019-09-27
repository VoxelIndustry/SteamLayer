package net.voxelindustry.steamlayer.network.action;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;
import net.voxelindustry.steamlayer.network.packet.ServerActionHolderPacket;

public class ServerActionBuilder
{
    private String actionKey;

    private TileEntity               tile;
    private CompoundNBT              payload;
    private ServerActionHolderPacket packet;

    public ServerActionBuilder(String actionKey)
    {
        this.actionKey = actionKey;
        payload = new CompoundNBT();
    }

    public ServerActionBuilder toTile(TileEntity tile)
    {
        this.tile = tile;
        packet = new ServerActionHolderPacket(this.tile, actionKey);
        return this;
    }

    public ServerActionBuilder withInt(String key, Integer value)
    {
        payload.putInt(key, value);
        return this;
    }

    public ServerActionBuilder withString(String key, String value)
    {
        payload.putString(key, value);
        return this;
    }

    public ServerActionBuilder withFloat(String key, Float value)
    {
        payload.putFloat(key, value);
        return this;
    }

    public ServerActionBuilder withDouble(String key, Double value)
    {
        payload.putDouble(key, value);
        return this;
    }

    public ServerActionBuilder withBoolean(String key, Boolean value)
    {
        payload.putBoolean(key, value);
        return this;
    }

    public ServerActionBuilder withItemStack(String key, ItemStack value)
    {
        payload.put(key, value.write(new CompoundNBT()));
        return this;
    }

    public ServerActionBuilder withFluidStack(String key, FluidStack value)
    {
        payload.put(key, value.writeToNBT(new CompoundNBT()));
        return this;
    }

    public ServerActionBuilder then(IActionCallback callback)
    {
        ActionManager.getInstance().addCallback(packet.getActionID(), callback);
        packet.setExpectAnswer(true);
        return this;
    }

    public void send()
    {
        if (packet == null)
            throw new RuntimeException("Action packet is null! Did you use setTile(TileEntity) before sending?");
        packet.setActionPayload(payload);
        SteamLayerPacketHandler.getHandler().sendToServer(packet);
    }
}

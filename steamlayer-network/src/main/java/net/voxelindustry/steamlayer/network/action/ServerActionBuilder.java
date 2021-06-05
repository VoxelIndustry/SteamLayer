package net.voxelindustry.steamlayer.network.action;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.voxelindustry.steamlayer.network.packet.ServerActionHolderPacket;

import static net.voxelindustry.steamlayer.network.SteamLayerClientPacketHandler.sendToServer;
import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.SERVER_ACTION_HOLDER;
import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.createServerBoundPacket;

public class ServerActionBuilder
{
    private String actionKey;

    private BlockEntity              tile;
    private NbtCompound              payload;
    private ServerActionHolderPacket packet;

    public ServerActionBuilder(String actionKey)
    {
        this.actionKey = actionKey;
        payload = new NbtCompound();
    }

    public ServerActionBuilder toTile(BlockEntity tile)
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
        payload.put(key, value.writeNbt(new NbtCompound()));
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
            throw new RuntimeException("Action packet is null! Did you use setTile(BlockEntity) before sending?");
        packet.setActionPayload(payload);
        sendToServer(createServerBoundPacket(SERVER_ACTION_HOLDER, buffer -> ServerActionHolderPacket.encode(packet, buffer)));
    }
}

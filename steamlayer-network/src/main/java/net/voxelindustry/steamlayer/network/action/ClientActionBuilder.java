package net.voxelindustry.steamlayer.network.action;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.voxelindustry.steamlayer.network.packet.ClientActionHolderPacket;

import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.*;

public class ClientActionBuilder
{
    private BlockEntity  sender;
    private PlayerEntity player;
    private NbtCompound  payload;
    private int          replyID;

    public ClientActionBuilder(int replyID, BlockEntity sender)
    {
        this.sender = sender;
        payload = new NbtCompound();
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
        payload.put(key, value.writeNbt(new NbtCompound()));
        return this;
    }

    public void send()
    {
        ClientActionHolderPacket packet = new ClientActionHolderPacket(replyID, payload);
        sendToPlayer(
                createClientBoundPacket(CLIENT_ACTION_HOLDER, buffer -> ClientActionHolderPacket.encode(packet, buffer)),
                (ServerPlayerEntity) player);
    }
}

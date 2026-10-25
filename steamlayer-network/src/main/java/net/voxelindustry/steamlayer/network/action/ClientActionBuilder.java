package net.voxelindustry.steamlayer.network.action;

import net.voxelindustry.steamlayer.common.utils.ItemUtils;
import net.voxelindustry.steamlayer.network.packet.ClientActionHolderPacket;

import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.*;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ClientActionBuilder
{
    private BlockEntity  sender;
    private Player player;
    private CompoundTag  payload;
    private int          replyID;

    public ClientActionBuilder(int replyID, BlockEntity sender)
    {
        this.sender = sender;
        payload = new CompoundTag();
        this.replyID = replyID;
    }

    public ClientActionBuilder toPlayer(Player player)
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
        payload.put(key, ItemUtils.saveStack(value, sender.getLevel().registryAccess()));
        return this;
    }

    public void send()
    {
        ClientActionHolderPacket packet = new ClientActionHolderPacket(replyID, payload);
        sendToPlayer(
                createClientBoundPacket(CLIENT_ACTION_HOLDER, buffer -> ClientActionHolderPacket.encode(packet, buffer)),
                (ServerPlayer) player);
    }
}

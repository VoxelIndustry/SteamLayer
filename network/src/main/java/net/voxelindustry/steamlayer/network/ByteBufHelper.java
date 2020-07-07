package net.voxelindustry.steamlayer.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ByteBufHelper
{
    public static void writeString(ByteBuf buffer, String value)
    {
        new PacketBuffer(buffer).writeString(value);
    }

    public static String readString(ByteBuf buffer)
    {
        return new PacketBuffer(buffer).readString();
    }

    public static void writeItemStack(ByteBuf buffer, ItemStack stack)
    {
        new PacketBuffer(buffer).writeItemStack(stack);
    }

    public static ItemStack readItemStack(ByteBuf buffer)
    {
        return new PacketBuffer(buffer).readItemStack();
    }

    public static void writePos(ByteBuf buffer, BlockPos pos)
    {
        new PacketBuffer(buffer).writeBlockPos(pos);
    }

    public static BlockPos readPos(ByteBuf buffer)
    {
        return new PacketBuffer(buffer).readBlockPos();
    }

    public static void writeTag(ByteBuf buffer, CompoundNBT tag)
    {
        new PacketBuffer(buffer).writeCompoundTag(tag);
    }

    public static CompoundNBT readTag(ByteBuf buffer)
    {
        return new PacketBuffer(buffer).readCompoundTag();
    }
}

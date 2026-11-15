package net.voxelindustry.steamlayer.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Consumer;

/**
 * Raw payload of a SteamLayer channel. Outgoing payloads write directly to the network buffer,
 * incoming payloads keep their bytes until a handler reads them with the receiver registries.
 */
public record SteamLayerPayload(Type<SteamLayerPayload> type,
                                Consumer<RegistryFriendlyByteBuf> writer,
                                byte[] data) implements CustomPacketPayload
{
    public static SteamLayerPayload outgoing(Type<SteamLayerPayload> type, Consumer<RegistryFriendlyByteBuf> writer)
    {
        return new SteamLayerPayload(type, writer, null);
    }

    public static StreamCodec<RegistryFriendlyByteBuf, SteamLayerPayload> codec(Type<SteamLayerPayload> type)
    {
        return StreamCodec.of(
                (buffer, payload) ->
                {
                    if (payload.writer() != null)
                        payload.writer().accept(buffer);
                    else
                        buffer.writeBytes(payload.data());
                },
                buffer ->
                {
                    byte[] data = new byte[buffer.readableBytes()];
                    buffer.readBytes(data);
                    return new SteamLayerPayload(type, null, data);
                });
    }

    public RegistryFriendlyByteBuf read(RegistryAccess registryAccess)
    {
        return new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), registryAccess);
    }
}

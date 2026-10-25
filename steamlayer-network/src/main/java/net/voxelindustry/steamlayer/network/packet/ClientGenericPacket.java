package net.voxelindustry.steamlayer.network.packet;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;

@Getter
@NoArgsConstructor
public class ClientGenericPacket
{
    private Message message;
    private int     packetID;

    public ClientGenericPacket(Message message)
    {
        this.message = message;
        packetID = PacketHandler.getInstance().getID(message.getClass());
    }

    public ClientGenericPacket decode(RegistryFriendlyByteBuf buffer)
    {
        packetID = buffer.readInt();

        try
        {
            message = PacketHandler.getInstance().byID(packetID).newInstance();
        } catch (InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
        }

        message.read(buffer);

        return this;
    }

    public void encode(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeInt(packetID);
        message.write(buffer);
    }
}

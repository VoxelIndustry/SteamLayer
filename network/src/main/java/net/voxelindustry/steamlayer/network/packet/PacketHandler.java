package net.voxelindustry.steamlayer.network.packet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class PacketHandler
{
    private BiMap<Integer, Class<? extends Message>> packets;

    private static PacketHandler INSTANCE;

    public static PacketHandler getInstance()
    {
        if (INSTANCE == null)
            INSTANCE = new PacketHandler();
        return INSTANCE;
    }

    private PacketHandler()
    {
        this.packets = HashBiMap.create();
    }

    public void register(Class<? extends Message> clazz)
    {
        packets.put(packets.size(), clazz);
    }

    public Class<? extends Message> byID(int packetID)
    {
        return packets.get(packetID);
    }

    public int getID(Class<? extends Message> clazz)
    {
        return packets.inverse().get(clazz);
    }
}

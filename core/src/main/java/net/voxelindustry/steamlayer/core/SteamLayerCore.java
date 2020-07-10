package net.voxelindustry.steamlayer.core;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.voxelindustry.steamlayer.container.sync.ContainerSyncPacket;
import net.voxelindustry.steamlayer.grid.GridManager;
import net.voxelindustry.steamlayer.network.SteamLayerPacketHandler;
import net.voxelindustry.steamlayer.network.packet.ClientActionHolderPacket;
import net.voxelindustry.steamlayer.network.packet.GenericPacket;
import net.voxelindustry.steamlayer.network.packet.PacketHandler;
import net.voxelindustry.steamlayer.network.packet.ServerActionHolderPacket;
import net.voxelindustry.steamlayer.network.packet.TileSyncRequestPacket;
import net.voxelindustry.steamlayer.tile.event.TileTickHandler;

import static net.voxelindustry.steamlayer.network.SteamLayerPacketHandler.*;

public class SteamLayerCore implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        registerPackets();
        PacketHandler.getInstance().register(ContainerSyncPacket.class);

        ClientTickEvents.START_WORLD_TICK.register(world -> ClientTickCounter.instance().tick(world));
        ServerTickEvents.END_SERVER_TICK.register(server -> TileTickHandler.tick());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> GridManager.onServerShutdown());
    }

    private void registerPackets()
    {
        SteamLayerPacketHandler.registerServerBoundHandler(GENERIC_PACKET,
                (buffer, context) -> GenericPacket.handle(GenericPacket.decode(buffer), context));

        SteamLayerPacketHandler.registerClientBoundHandler(GENERIC_PACKET,
                (buffer, context) -> GenericPacket.handle(GenericPacket.decode(buffer), context));

        SteamLayerPacketHandler.registerServerBoundHandler(SERVER_ACTION_HOLDER,
                (buffer, context) -> ServerActionHolderPacket.handle(ServerActionHolderPacket.decode(buffer), context));

        SteamLayerPacketHandler.registerClientBoundHandler(CLIENT_ACTION_HOLDER,
                (buffer, context) -> ClientActionHolderPacket.handle(ClientActionHolderPacket.decode(buffer), context));

        SteamLayerPacketHandler.registerServerBoundHandler(TILE_SYNC_REQUEST,
                (buffer, context) -> TileSyncRequestPacket.handle(TileSyncRequestPacket.decode(buffer), context));
    }
}

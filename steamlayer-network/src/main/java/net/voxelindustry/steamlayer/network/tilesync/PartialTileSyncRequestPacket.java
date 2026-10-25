package net.voxelindustry.steamlayer.network.tilesync;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.voxelindustry.steamlayer.network.packet.Message;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PartialTileSyncRequestPacket extends Message
{
    private BlockPos               pos;
    private ResourceLocation             dimension;
    private Collection<ResourceLocation> elements;

    @Override
    public void read(FriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
        dimension = buf.readResourceLocation();

        elements = new ArrayList<>();
        int elementSize = buf.readInt();
        for (int i = 0; i < elementSize; i++)
            elements.add(buf.readResourceLocation());
    }

    @Override
    public void write(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeResourceLocation(dimension);
        buf.writeInt(elements.size());
        elements.forEach(buf::writeResourceLocation);
    }

    @Override
    public void handle(Player player)
    {
        Level world = player.getCommandSenderWorld();

        if (!world.dimension().location().equals(dimension) || !world.hasChunkAt(pos))
            return;

        BlockEntity tile = world.getBlockEntity(pos);

        if (!(tile instanceof PartialSyncedTile))
            return;

        elements.forEach(element -> PartialTileSync.syncPart((PartialSyncedTile) tile, tile, element));
    }
}

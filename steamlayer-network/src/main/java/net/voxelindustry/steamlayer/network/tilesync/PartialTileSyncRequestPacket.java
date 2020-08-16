package net.voxelindustry.steamlayer.network.tilesync;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.voxelindustry.steamlayer.network.packet.Message;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PartialTileSyncRequestPacket extends Message
{
    private BlockPos               pos;
    private Identifier             dimension;
    private Collection<Identifier> elements;

    @Override
    public void read(PacketByteBuf buf)
    {
        pos = buf.readBlockPos();
        dimension = buf.readIdentifier();

        elements = new ArrayList<>();
        int elementSize = buf.readInt();
        for (int i = 0; i < elementSize; i++)
            elements.add(buf.readIdentifier());
    }

    @Override
    public void write(PacketByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeIdentifier(dimension);
        buf.writeInt(elements.size());
        elements.forEach(buf::writeIdentifier);
    }

    @Override
    public void handle(PlayerEntity player)
    {
        World world = player.getEntityWorld();

        if (!world.getRegistryKey().getValue().equals(dimension) || !world.isChunkLoaded(pos))
            return;

        BlockEntity tile = world.getBlockEntity(pos);

        if (!(tile instanceof PartialSyncedTile))
            return;

        elements.forEach(element -> PartialTileSync.syncPart((PartialSyncedTile) tile, tile, element));
    }
}

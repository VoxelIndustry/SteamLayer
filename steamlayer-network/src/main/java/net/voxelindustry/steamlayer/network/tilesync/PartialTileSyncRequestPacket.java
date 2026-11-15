package net.voxelindustry.steamlayer.network.tilesync;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
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
    private Identifier             dimension;
    private Collection<Identifier> elements;

    @Override
    public void read(RegistryFriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
        dimension = buf.readIdentifier();

        elements = new ArrayList<>();
        int elementSize = buf.readInt();
        for (int i = 0; i < elementSize; i++)
            elements.add(buf.readIdentifier());
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeIdentifier(dimension);
        buf.writeInt(elements.size());
        elements.forEach(buf::writeIdentifier);
    }

    @Override
    public void handle(Player player)
    {
        Level world = player.level();

        if (!world.dimension().identifier().equals(dimension) || !world.hasChunkAt(pos))
            return;

        BlockEntity tile = world.getBlockEntity(pos);

        if (!(tile instanceof PartialSyncedTile))
            return;

        elements.forEach(element -> PartialTileSync.syncPart((PartialSyncedTile) tile, tile, element));
    }
}

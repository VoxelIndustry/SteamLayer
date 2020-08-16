package net.voxelindustry.steamlayer.network.tilesync;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.voxelindustry.steamlayer.network.packet.Message;

@Log4j2
@NoArgsConstructor
@AllArgsConstructor
public class PartialTileSyncPacket extends Message
{
    private BlockPos    pos;
    private Identifier  dimension;
    private CompoundTag tag;
    private Identifier  elementIdentifier;

    @Override
    public void read(PacketByteBuf buf)
    {
        pos = buf.readBlockPos();
        dimension = buf.readIdentifier();
        tag = buf.readCompoundTag();
        elementIdentifier = buf.readIdentifier();
    }

    @Override
    public void write(PacketByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeIdentifier(dimension);
        buf.writeCompoundTag(tag);
        buf.writeIdentifier(elementIdentifier);
    }

    @Override
    public void handle(PlayerEntity player)
    {
        World world = player.getEntityWorld();

        if (!world.getRegistryKey().getValue().equals(dimension) || !world.isChunkLoaded(pos))
            return;

        PartialSyncedTile tile = (PartialSyncedTile) world.getBlockEntity(pos);

        if (tile == null)
            return;

        PartialTileSync.receiveSync(tile, elementIdentifier, tag);
    }
}

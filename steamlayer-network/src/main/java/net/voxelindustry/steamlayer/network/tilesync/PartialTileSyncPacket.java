package net.voxelindustry.steamlayer.network.tilesync;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.voxelindustry.steamlayer.network.packet.Message;

@Log4j2
@NoArgsConstructor
@AllArgsConstructor
public class PartialTileSyncPacket extends Message
{
    private BlockPos pos;
    private Identifier dimension;
    private CompoundTag tag;
    private Identifier elementIdentifier;

    @Override
    public void read(RegistryFriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
        dimension = buf.readIdentifier();
        tag = buf.readNbt();
        elementIdentifier = buf.readIdentifier();
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeIdentifier(dimension);
        buf.writeNbt(tag);
        buf.writeIdentifier(elementIdentifier);
    }

    @Override
    public void handle(Player player)
    {
        Level world = player.level();

        if (!world.dimension().identifier().equals(dimension) || !world.hasChunkAt(pos))
            return;

        var blockEntity = world.getBlockEntity(pos);
        var tile = (PartialSyncedTile) blockEntity;

        if (tile == null)
            return;

        PartialTileSync.receiveSync(tile, blockEntity, elementIdentifier, tag);
    }
}

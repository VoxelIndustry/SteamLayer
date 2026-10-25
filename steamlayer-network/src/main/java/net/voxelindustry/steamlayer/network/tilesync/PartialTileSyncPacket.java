package net.voxelindustry.steamlayer.network.tilesync;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.voxelindustry.steamlayer.network.packet.Message;

@Log4j2
@NoArgsConstructor
@AllArgsConstructor
public class PartialTileSyncPacket extends Message
{
    private BlockPos pos;
    private ResourceLocation dimension;
    private CompoundTag tag;
    private ResourceLocation elementIdentifier;

    @Override
    public void read(FriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
        dimension = buf.readResourceLocation();
        tag = buf.readNbt();
        elementIdentifier = buf.readResourceLocation();
    }

    @Override
    public void write(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeResourceLocation(dimension);
        buf.writeNbt(tag);
        buf.writeResourceLocation(elementIdentifier);
    }

    @Override
    public void handle(Player player)
    {
        Level world = player.getCommandSenderWorld();

        if (!world.dimension().location().equals(dimension) || !world.hasChunkAt(pos))
            return;

        var blockEntity = world.getBlockEntity(pos);
        var tile = (PartialSyncedTile) blockEntity;

        if (tile == null)
            return;

        PartialTileSync.receiveSync(tile, blockEntity, elementIdentifier, tag);
    }
}

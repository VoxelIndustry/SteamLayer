package net.voxelindustry.steamlayer.network.action;

import lombok.Data;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;

@Data
public class ActionSender
{
    private PlayerEntity player;
    private TileEntity   receiver;
    private int          actionID;
    private boolean      answered;

    public ActionSender(PlayerEntity player, TileEntity receiver, int actionID)
    {
        this.player = player;
        this.receiver = receiver;
        this.actionID = actionID;
    }

    public ClientActionBuilder answer()
    {
        this.answered = true;
        return new ClientActionBuilder(actionID, receiver).toPlayer(player);
    }
}

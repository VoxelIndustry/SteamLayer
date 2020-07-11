package net.voxelindustry.steamlayer.network.action;

import lombok.Data;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;

@Data
public class ActionSender
{
    private PlayerEntity player;
    private BlockEntity  receiver;
    private int          actionID;
    private boolean      answered;

    public ActionSender(PlayerEntity player, BlockEntity receiver, int actionID)
    {
        this.player = player;
        this.receiver = receiver;
        this.actionID = actionID;
    }

    public ClientActionBuilder answer()
    {
        answered = true;
        return new ClientActionBuilder(actionID, receiver).toPlayer(player);
    }
}

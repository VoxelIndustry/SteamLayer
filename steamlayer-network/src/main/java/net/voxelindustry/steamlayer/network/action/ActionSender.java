package net.voxelindustry.steamlayer.network.action;

import lombok.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

@Data
public class ActionSender
{
    private Player player;
    private BlockEntity  receiver;
    private int          actionID;
    private boolean      answered;

    public ActionSender(Player player, BlockEntity receiver, int actionID)
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

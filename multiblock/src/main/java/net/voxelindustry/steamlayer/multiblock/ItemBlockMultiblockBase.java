package net.voxelindustry.steamlayer.multiblock;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class ItemBlockMultiblockBase extends BlockItem
{
    public ItemBlockMultiblockBase(Block block, Item.Settings builder)
    {
        super(block, builder);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context)
    {
        BlockState blockState = context.getWorld().getBlockState(context.getBlockPos());
        Block block = blockState.getBlock();
        BlockPos pos = context.getBlockPos();

        ItemPlacementContext blockContext = getPlacementContext(new ItemPlacementContext(context));
        if (!block.canReplace(blockState, blockContext))
            pos = pos.offset(context.getSide());

        ItemStack itemstack = context.getPlayer().getStackInHand(context.getHand());
        BlockMultiblockBase base = (BlockMultiblockBase) getBlock();

        if (!itemstack.isEmpty() && context.getPlayer().canPlaceOn(pos, context.getSide(), itemstack)
                && base.canPlaceBlockAt(context.getWorld(), pos, context.getPlayer().getHorizontalFacing().getOpposite()))
        {
            BlockState state = ((BlockMultiblockBase<?>) getBlock()).getStateForPlacement(new BlockItemUseContext(context));

            if (place(blockContext, state))
            {
                BlockState currentState = context.getWorld().getBlockState(pos);

                postPlacement(pos, context.getWorld(), context.getPlayer(), itemstack, currentState);
                block.onPlaced(context.getWorld(), pos, currentState, context.getPlayer(), itemstack);

                BlockSoundGroup sound = currentState.getSoundGroup();
                context.getWorld().playSound(context.getPlayer(), pos, sound.getPlaceSound(), SoundCategory.BLOCKS,
                                             (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                itemstack.decrement(1);

                if (context.getPlayer() instanceof ServerPlayerEntity)
                    Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity) context.getPlayer(), pos, itemstack);
            }
            return ActionResult.SUCCESS;
        } else
            return ActionResult.FAIL;
    }
}

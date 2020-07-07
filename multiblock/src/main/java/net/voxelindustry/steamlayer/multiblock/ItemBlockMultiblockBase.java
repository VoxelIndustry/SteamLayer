package net.voxelindustry.steamlayer.multiblock;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class ItemBlockMultiblockBase extends BlockItem
{
    public ItemBlockMultiblockBase(Block block, Item.Properties builder)
    {
        super(block, builder);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        BlockState blockState = context.getWorld().getBlockState(context.getPos());
        Block block = blockState.getBlock();
        BlockPos pos = context.getPos();

        BlockItemUseContext blockContext = getBlockItemUseContext(new BlockItemUseContext(context));
        if (!block.isReplaceable(blockState, blockContext))
            pos = pos.offset(context.getFace());

        ItemStack itemstack = context.getPlayer().getHeldItem(context.getHand());
        final BlockMultiblockBase base = (BlockMultiblockBase) this.getBlock();

        if (!itemstack.isEmpty() && context.getPlayer().canPlayerEdit(pos, context.getFace(), itemstack)
                && base.canPlaceBlockAt(context.getWorld(), pos, context.getPlayer().getHorizontalFacing().getOpposite()))
        {
            BlockState state = this.getBlock().getStateForPlacement(new BlockItemUseContext(context));

            if (placeBlock(blockContext, state))
            {
                BlockState currentState = context.getWorld().getBlockState(pos);

                this.onBlockPlaced(pos, context.getWorld(), context.getPlayer(), itemstack, currentState);
                block.onBlockPlacedBy(context.getWorld(), pos, currentState, context.getPlayer(), itemstack);

                SoundType soundtype = currentState.getSoundType(context.getWorld(), pos, context.getPlayer());
                context.getWorld().playSound(context.getPlayer(), pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
                        (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                itemstack.shrink(1);

                if (context.getPlayer() instanceof ServerPlayerEntity)
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) context.getPlayer(), pos, itemstack);
            }
            return ActionResultType.SUCCESS;
        }
        else
            return ActionResultType.FAIL;
    }
}

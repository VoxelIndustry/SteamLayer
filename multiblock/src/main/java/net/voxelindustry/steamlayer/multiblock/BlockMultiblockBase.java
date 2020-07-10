package net.voxelindustry.steamlayer.multiblock;

import com.sun.istack.internal.Nullable;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.data.client.model.VariantSettings.Rotation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.voxelindustry.steamlayer.tile.TileBase;
import net.voxelindustry.steamlayer.tile.descriptor.ModularTiles;

import java.util.List;

import static java.util.Collections.emptyList;

public abstract class BlockMultiblockBase<T extends TileBase & ITileMultiblockCore> extends Block implements IBlockEntityProvider
{
    public static final BooleanProperty   MULTIBLOCK_GAG = BooleanProperty.create("multiblockgag");
    public static final DirectionProperty FACING         = Properties.HORIZONTAL_FACING;

    private final String   modClass;
    private final Class<T> tileClass;

    private MultiblockComponent multiblock;

    public BlockMultiblockBase(String modClass, AbstractBlock.Settings settings, Class<T> tileClass)
    {
        super(settings);

        this.modClass = modClass;
        this.tileClass = tileClass;

        setDefaultState(getStateManager().getDefaultState()
                                .with(BlockMultiblockBase.MULTIBLOCK_GAG, false)
                                .with(BlockMultiblockBase.FACING, Direction.NORTH));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        if (state.get(BlockMultiblockBase.MULTIBLOCK_GAG))
            return BlockRenderType.INVISIBLE;
        return BlockRenderType.MODEL;
    }

    public static Direction getFacing(BlockState state)
    {
        return state.get(BlockMultiblockBase.FACING);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(MULTIBLOCK_GAG, FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
    {
        if (!state.get(BlockMultiblockBase.MULTIBLOCK_GAG))
            return getMultiblock().getBox(BlockMultiblockBase.getFacing(state));

        if (world.getBlockEntity(pos) instanceof ITileMultiblock)
        {
            ITileMultiblock tile = (ITileMultiblock) world.getBlockEntity(pos);
            if (tile != null && !tile.isCore())
                return world.getBlockState(tile.getCorePos()).getOutlineShape(world, tile.getCorePos(), context)
                        .offset(tile.getCoreOffset().getX(), tile.getCoreOffset().getY(), tile.getCoreOffset().getZ());
        }
        return VoxelShapes.fullCube();
    }

    public boolean canPlaceBlockAt(World w, BlockPos pos, Direction facing)
    {
        BlockPos[] searchables = getMultiblock().getAllInBox(pos, facing).toArray(BlockPos[]::new);

        for (BlockPos current : searchables)
        {
            if (!w.getBlockState(current).getMaterial().isReplaceable())
                return false;
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World w, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        BlockPos[] searchables = getMultiblock().getAllInBox(pos,
                                                             BlockMultiblockBase.getFacing(state)).toArray(BlockPos[]::new);

        for (BlockPos current : searchables)
        {
            if (!current.equals(pos))
            {
                BlockState previous = w.getBlockState(current);
                w.setBlockState(current, getDefaultState().with(BlockMultiblockBase.MULTIBLOCK_GAG, true));
                w.updateListeners(current, previous, w.getBlockState(current), 3);
                TileMultiblockGag gag = (TileMultiblockGag) w.getBlockEntity(current);
                if (gag != null)
                    gag.setCorePos(pos);
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
    {
        ITileMultiblock tile = (ITileMultiblock) world.getBlockEntity(pos);
        if (tile != null)
        {
            if (tile.isCore())
            {
                if (tile instanceof Inventory)
                {
                    ItemScatterer.spawn(world, pos, (Inventory) tile);
                    world.updateComparators(pos, this);
                }
            } else
                tile.breakCore();
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, Builder builder)
    {
        if (state.get(MULTIBLOCK_GAG))
            return super.getDroppedStacks(state, builder);
        return emptyList();
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);

        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileMultiblockGag && !((TileMultiblockGag) tile).isCorePresent())
            world.getBlockEntity(pos).getWorld().breakBlock(pos, false);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
    {
        ITileMultiblock tile = (ITileMultiblock) world.getBlockEntity(pos);

        if (tile != null)
            return tile.getCore().onRightClick(player, hit, tile.getCoreOffset());
        return ActionResult.PASS;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot)
    {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return getDefaultState()
                .with(BlockMultiblockBase.MULTIBLOCK_GAG, false)
                .with(BlockMultiblockBase.FACING, context.getPlacementHorizontalFacing().getOpposite());
    }


    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world)
    {
        if (state.get(BlockMultiblockBase.MULTIBLOCK_GAG))
            return new TileMultiblockGag();
        return getTile(world, state);
    }

    public abstract T getTile(World w, BlockState state)

    public MultiblockComponent getMultiblock()
    {
        if (multiblock == null)
            multiblock = ModularTiles.instance(modClass).getComponent(MultiblockComponent.class,
                                                                      getRegistryName().getPath());
        return multiblock;
    }

    ////////////////////
    // TILE RETRIEVAL //
    ////////////////////

    public T getWorldTile(IBlockReader world, BlockPos pos)
    {
        return (T) getRawWorldTile(world, pos);
    }

    public BlockEntity getRawWorldTile(IBlockReader world, BlockPos pos)
    {
        return world.getBlockEntity(pos);
    }

    public boolean checkWorldTile(IBlockReader world, BlockPos pos)
    {
        return tileClass.isInstance(world.getBlockEntity(pos));
    }

    public Class<T> getTileClass()
    {
        return tileClass;
    }
}

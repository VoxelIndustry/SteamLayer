package net.voxelindustry.steamlayer.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.voxelindustry.steamlayer.tile.TileBase;
import net.voxelindustry.steamlayer.tile.descriptor.ModularTiles;

import javax.annotation.Nullable;
import java.util.List;

import static java.util.Collections.emptyList;

public abstract class BlockMultiblockBase<T extends TileBase & ITileMultiblockCore> extends Block implements ITileEntityProvider
{
    public static final BooleanProperty   MULTIBLOCK_GAG = BooleanProperty.create("multiblockgag");
    public static final DirectionProperty FACING         = HorizontalBlock.HORIZONTAL_FACING;

    private final String   modClass;
    private final Class<T> tileClass;

    private MultiblockComponent multiblock;

    public BlockMultiblockBase(String modClass, Block.Properties properties, Class<T> tileClass)
    {
        super(properties);

        this.modClass = modClass;
        this.tileClass = tileClass;

        setDefaultState(getStateContainer().getBaseState()
                .with(BlockMultiblockBase.MULTIBLOCK_GAG, false)
                .with(BlockMultiblockBase.FACING, Direction.NORTH));
    }

    @Override
    public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos)
    {
        return false;
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
    public VoxelShape getShape(BlockState state, IBlockReader w, BlockPos pos, ISelectionContext context)
    {
        if (!state.get(BlockMultiblockBase.MULTIBLOCK_GAG))
            return getMultiblock().getBox(BlockMultiblockBase.getFacing(state));

        if (w.getTileEntity(pos) instanceof ITileMultiblock)
        {
            ITileMultiblock tile = (ITileMultiblock) w.getTileEntity(pos);
            if (tile != null && !tile.isCore())
                return w.getBlockState(tile.getCorePos()).getShape(w, tile.getCorePos(), context)
                        .withOffset(tile.getCoreOffset().getX(), tile.getCoreOffset().getY(), tile.getCoreOffset().getZ());
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
    public void onBlockPlacedBy(World w, BlockPos pos, BlockState state,
                                LivingEntity placer, ItemStack stack)
    {
        BlockPos[] searchables = getMultiblock().getAllInBox(pos,
                BlockMultiblockBase.getFacing(state)).toArray(BlockPos[]::new);

        for (BlockPos current : searchables)
        {
            if (!current.equals(pos))
            {
                BlockState previous = w.getBlockState(current);
                w.setBlockState(current, getDefaultState().with(BlockMultiblockBase.MULTIBLOCK_GAG, true));
                w.notifyBlockUpdate(current, previous, w.getBlockState(current), 3);
                TileMultiblockGag gag = (TileMultiblockGag) w.getTileEntity(current);
                if (gag != null)
                    gag.setCorePos(pos);
            }
        }
    }

    @Override
    public void onReplaced(BlockState state, World w, BlockPos pos, BlockState newState, boolean isMoving)
    {
        ITileMultiblock tile = (ITileMultiblock) w.getTileEntity(pos);
        if (tile != null)
        {
            if (tile.isCore())
            {
                if (tile instanceof IInventory)
                {
                    InventoryHelper.dropInventoryItems(w, pos, (IInventory) tile);
                    w.updateComparatorOutputLevel(pos, this);
                }
            }
            else
                tile.breakCore();
        }
        super.onReplaced(state, w, pos, newState, isMoving);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
    {
        if (state.get(MULTIBLOCK_GAG))
            return super.getDrops(state, builder);
        return emptyList();
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader w, BlockPos pos, BlockPos from)
    {
        super.onNeighborChange(state, w, pos, from);

        TileEntity tile = w.getTileEntity(pos);
        if (tile instanceof TileMultiblockGag && !((TileMultiblockGag) tile).isCorePresent())
            w.getTileEntity(pos).getWorld().destroyBlock(pos, false);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World w, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
    {
        ITileMultiblock tile = (ITileMultiblock) w.getTileEntity(pos);

        if (tile != null)
            return tile.getCore().onRightClick(player, hit, tile.getCoreOffset());
        return false;
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
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        if (state.get(BlockMultiblockBase.MULTIBLOCK_GAG))
            return new TileMultiblockGag();
        return getTile(world, state);
    }

    public abstract T getTile(IBlockReader w, BlockState state);

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

    public TileEntity getRawWorldTile(IBlockReader world, BlockPos pos)
    {
        return world.getTileEntity(pos);
    }

    public boolean checkWorldTile(IBlockReader world, BlockPos pos)
    {
        return tileClass.isInstance(world.getTileEntity(pos));
    }

    public Class<T> getTileClass()
    {
        return tileClass;
    }
}

package net.voxelindustry.steamlayer.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.voxelindustry.steamlayer.tile.TileBase;
import net.voxelindustry.steamlayer.tile.descriptor.ModularTiles;

import javax.annotation.Nullable;

public abstract class BlockMultiblockBase<T extends TileBase & ITileMultiblockCore> extends Block implements ITileEntityProvider
{
    public static final PropertyBool      MULTIBLOCK_GAG = PropertyBool.create("multiblockgag");
    public static final PropertyDirection FACING         = BlockHorizontal.FACING;

    private final String   modClass;
    private final Class<T> tileClass;

    private MultiblockComponent multiblock;

    public BlockMultiblockBase(String modClass, Material material, Class<T> tileClass)
    {
        super(material);

        this.modClass = modClass;
        this.tileClass = tileClass;

        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockMultiblockBase.MULTIBLOCK_GAG, false)
                .withProperty(BlockMultiblockBase.FACING, EnumFacing.NORTH));
    }

    @Override
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        if (state.getValue(BlockMultiblockBase.MULTIBLOCK_GAG))
            return EnumBlockRenderType.INVISIBLE;
        return EnumBlockRenderType.MODEL;
    }

    @Nullable
    public static EnumFacing getFacing(int meta)
    {
        int i = meta & 7;
        return i > 5 ? null : EnumFacing.byIndex(i);
    }

    public static EnumFacing getFacing(IBlockState state)
    {
        return state.getValue(BlockMultiblockBase.FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(BlockMultiblockBase.FACING, BlockMultiblockBase.getFacing(meta))
                .withProperty(BlockMultiblockBase.MULTIBLOCK_GAG, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;
        i = i | state.getValue(BlockMultiblockBase.FACING).getIndex();

        if (state.getValue(BlockMultiblockBase.MULTIBLOCK_GAG))
            i |= 8;
        return i;
    }

    @Override
    public BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, MULTIBLOCK_GAG, FACING);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World w, BlockPos pos)
    {
        if (!state.getValue(BlockMultiblockBase.MULTIBLOCK_GAG))
            return this.getMultiblock().getBox(BlockMultiblockBase.getFacing(state)).offset(pos);

        if (w.getTileEntity(pos) instanceof ITileMultiblock)
        {
            ITileMultiblock tile = (ITileMultiblock) w.getTileEntity(pos);
            if (tile != null && !tile.isCore())
                return w.getBlockState(tile.getCorePos()).getSelectedBoundingBox(w, tile.getCorePos());
        }
        return Block.FULL_BLOCK_AABB.offset(pos);
    }

    public boolean canPlaceBlockAt(World w, BlockPos pos, EnumFacing facing)
    {
        Iterable<BlockPos> searchables = this.getMultiblock().getAllInBox(pos, facing);

        for (BlockPos current : searchables)
        {
            if (!w.getBlockState(current).getBlock().isReplaceable(w, current))
                return false;
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World w, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack)
    {
        Iterable<BlockPos> searchables = this.getMultiblock().getAllInBox(pos, BlockMultiblockBase.getFacing
                (state));

        for (BlockPos current : searchables)
        {
            if (!current.equals(pos))
            {
                IBlockState previous = w.getBlockState(current);
                w.setBlockState(current, this.getDefaultState().withProperty(BlockMultiblockBase.MULTIBLOCK_GAG, true));
                w.notifyBlockUpdate(current, previous, w.getBlockState(current), 3);
                TileMultiblockGag gag = (TileMultiblockGag) w.getTileEntity(current);
                if (gag != null)
                    gag.setCorePos(pos);
            }
        }
    }

    @Override
    public void breakBlock(World w, BlockPos pos, IBlockState state)
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
                this.dropBlockAsItem(w, pos, state, 0);
            }
            else
                tile.breakCore();
        }
        super.breakBlock(w, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World w, BlockPos pos, Block block, BlockPos from)
    {
        super.neighborChanged(state, w, pos, block, from);

        TileEntity tile = w.getTileEntity(pos);
        if (tile instanceof TileMultiblockGag && !((TileMultiblockGag) tile).isCorePresent())
            w.getTileEntity(pos).getWorld().destroyBlock(pos, false);
    }

    @Override
    public boolean onBlockActivated(World w, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ITileMultiblock tile = (ITileMultiblock) w.getTileEntity(pos);

        if (tile != null)
            return tile.getCore().onRightClick(player, facing, hitX, hitY, hitZ, tile.getCoreOffset());
        return false;
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(BlockMultiblockBase.FACING, rot.rotate(state.getValue(BlockMultiblockBase.FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(BlockMultiblockBase.FACING)));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(BlockMultiblockBase.MULTIBLOCK_GAG, false)
                .withProperty(BlockMultiblockBase.FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public TileEntity createNewTileEntity(World w, int meta)
    {
        IBlockState state = this.getStateFromMeta(meta);
        if (state.getValue(BlockMultiblockBase.MULTIBLOCK_GAG))
            return new TileMultiblockGag();
        return this.getTile(w, state);
    }

    public abstract T getTile(World w, IBlockState state);

    public MultiblockComponent getMultiblock()
    {
        if (this.multiblock == null)
            this.multiblock = ModularTiles.instance(modClass).getComponent(MultiblockComponent.class,
                    this.getRegistryName().getPath());
        return this.multiblock;
    }

    public boolean isWordTileCore(IBlockAccess world, BlockPos pos)
    {
        if (world instanceof ChunkCache)
            return ((ChunkCache) world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK)
                    instanceof ITileMultiblockCore;
        else
            return world.getTileEntity(pos) instanceof ITileMultiblockCore;
    }

    ////////////////////
    // TILE RETRIEVAL //
    ////////////////////

    public T getWorldTile(IBlockAccess world, BlockPos pos)
    {
        return (T) this.getRawWorldTile(world, pos);
    }

    public TileEntity getRawWorldTile(IBlockAccess world, BlockPos pos)
    {
        if (world instanceof ChunkCache)
            return ((ChunkCache) world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        else
            return world.getTileEntity(pos);
    }

    public boolean checkWorldTile(IBlockAccess world, BlockPos pos)
    {
        if (world instanceof ChunkCache)
            return tileClass.isInstance(((ChunkCache) world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK));
        else
            return tileClass.isInstance(world.getTileEntity(pos));
    }

    public Class<T> getTileClass()
    {
        return tileClass;
    }
}

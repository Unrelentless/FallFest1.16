package com.unrelentless.fallfest116.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;

public class FallenGrassBlock extends Block {

    public enum LeafType implements StringIdentifiable {
        ACACIA, BIRCH, DARK_OAK, JUNGLE, OAK, SPRUCE;

        @Override
        public String asString() {
            switch (this) {
                case ACACIA:
                    return "acacia";
                case BIRCH:
                    return "birch";
                case DARK_OAK:
                    return "dark_oak";
                case JUNGLE:
                    return "jungle";
                case OAK:
                    return "oak";
                case SPRUCE:
                    return "spruce";
                default:
                    return "oak";
            }
        }

        public static LeafType typeForBiome(Biome.Category biome) {
            switch (biome) {
                case DESERT:
                case MESA:
                case SAVANNA:
                    return ACACIA;
                case RIVER:
                case BEACH:
                    return BIRCH;
                case SWAMP:
                case FOREST:
                case MUSHROOM:
                    return DARK_OAK;
                case JUNGLE:
                case OCEAN:
                    return JUNGLE;
                case EXTREME_HILLS:
                case ICY:
                case TAIGA:
                    return SPRUCE;
                case THEEND:
                case NETHER:
                case NONE:
                case PLAINS:
                default:
                    return OAK;
            }
        }
    }

    public static final EnumProperty<LeafType> TYPE = EnumProperty.of("type", LeafType.class);

    public FallenGrassBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(TYPE, LeafType.OAK));
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    }

    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    }

    public VoxelShape getVisualShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        switch (type) {
            case LAND:
                return true;
            case WATER:
            case AIR:
            default:
                return false;
        }
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    public int getOpacity(BlockState state, BlockView world, BlockPos pos) {
        return 1;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos.down());
        if (!blockState.isOf(Blocks.ICE) && !blockState.isOf(Blocks.PACKED_ICE) && !blockState.isOf(Blocks.BARRIER)) {
            if (!blockState.isOf(Blocks.HONEY_BLOCK) && !blockState.isOf(Blocks.SOUL_SAND)) {
                boolean should = Block.isFaceFullSquare(blockState.getCollisionShape(world, pos.down()), Direction.UP)
                        && blockState.getBlock() != this;
                return should;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
            WorldAccess world, BlockPos pos, BlockPos posFrom) {
        return !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        if (context.getStack().getItem() == this.asItem()) {
            if (context.canReplaceExisting()) {
                return context.getSide() == Direction.UP;
            } else {
                return true;
            }
        }
        return true;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }
}
package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.block.RopeBlock;

import javax.annotation.Nullable;

public class RopeItem extends BlockItem {
    public RopeItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Nullable
    @Override
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        BlockPos blockpos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState blockstate = level.getBlockState(blockpos);
        Block block = this.getBlock();

        if (!blockstate.is(block)) {
            return context;
        } else {
            Direction direction = this.getForward(context, blockstate);

            int i = 0;
            BlockPos.MutableBlockPos mutableBlockPos = blockpos.mutable();

            while(i < 7) {
                if (!level.isClientSide && !level.isInWorldBounds(mutableBlockPos)) {
                    Player player = context.getPlayer();
                    int j = level.getMaxBuildHeight();
                    if (player instanceof ServerPlayer && mutableBlockPos.getY() >= j) {
                        ((ServerPlayer)player).sendSystemMessage(Component.translatable("build.tooHigh", j - 1).withStyle(ChatFormatting.RED), true);
                    }
                    break;
                }

                blockstate = level.getBlockState(mutableBlockPos);
                if (!blockstate.is(this.getBlock())) {
                    if (blockstate.canBeReplaced(context)) return BlockPlaceContext.at(context, mutableBlockPos, direction);
                    break;
                } else {
                    if (!stateHasValue(blockstate, direction)) return BlockPlaceContext.at(context, mutableBlockPos, direction);
                }

                mutableBlockPos.move(direction);
                if (direction.getAxis().isHorizontal()) {
                    ++i;
                }
            }

            return null;
        }
    }

    protected boolean stateHasValue(BlockState state, Direction direction) {
        return switch (direction.getAxis()) {
            case X -> state.getValue(RopeBlock.X);
            case Y -> state.getValue(RopeBlock.Y);
            default -> state.getValue(RopeBlock.Z);
        };
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);


        if (state.is(this.getBlock())) {
            Direction direction = context.getClickedFace();
            if (direction.getAxis() == Direction.Axis.X && !state.getValue(RopeBlock.X)) {
                return state.setValue(RopeBlock.X, true);
            } else if (direction.getAxis() == Direction.Axis.Y && !state.getValue(RopeBlock.Y)) {
                return state.setValue(RopeBlock.Y, true);
            } else if (direction.getAxis() == Direction.Axis.Z && !state.getValue(RopeBlock.Z)) {
                return state.setValue(RopeBlock.Z, true);
            } else {
                //TODO
            }
        }

        return super.getPlacementState(context);
    }

    protected Direction getForward(BlockPlaceContext context, BlockState state) {
        if (context.isSecondaryUseActive()) {
            return context.isInside() ? context.getClickedFace().getOpposite() : context.getClickedFace();//TODO
        } else {
            for (Direction dir : context.getNearestLookingDirections()) {
                switch (dir.getAxis()) {
                    case X -> {
                        if (state.getValue(RopeBlock.X)) return dir;
                    }
                    case Y -> {
                        if (state.getValue(RopeBlock.Y)) return Direction.DOWN; // Ropes don't go up
                    }
                    case Z -> {
                        if (state.getValue(RopeBlock.Z)) return dir;
                    }
                }
            }
            return Direction.DOWN;
        }
    }

    @Override
    protected boolean mustSurvive() {
        return false;
    }
}

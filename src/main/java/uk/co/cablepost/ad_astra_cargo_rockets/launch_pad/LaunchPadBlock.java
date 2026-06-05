package uk.co.cablepost.ad_astra_cargo_rockets.launch_pad;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import javax.annotation.Nullable;
import uk.co.cablepost.ad_astra_cargo_rockets.AdAstraCargoRockets;
import uk.co.cablepost.ad_astra_cargo_rockets.CargoRocketItem;
import uk.co.cablepost.ad_astra_cargo_rockets.cargo_rocket.CargoRocketEntity;

import java.util.List;

public class LaunchPadBlock extends BaseEntityBlock {

    public LaunchPadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaunchPadBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, AdAstraCargoRockets.LAUNCH_PAD.getBlockEntity().get(),
                LaunchPadBlockEntity::tick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && !stack.isEmpty() && stack.getItem() instanceof CargoRocketItem cargoRocketItem) {
            List<CargoRocketEntity> nearby = level.getEntitiesOfClass(
                    CargoRocketEntity.class, new AABB(pos).inflate(3), CargoRocketEntity::isAlive);
            if (nearby.isEmpty()) {
                CargoRocketEntity entity = AdAstraCargoRockets.CARGO_ROCKET_ENTITY.get().create(level);
                if (entity != null) {
                    entity.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0f, 0f);
                    level.addFreshEntity(entity);
                    entity.setTier(cargoRocketItem.tier);
                    stack.shrink(1);
                    return InteractionResult.CONSUME;
                }
            }
        }

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LaunchPadBlockEntity launchPad) {
                NetworkHooks.openScreen((ServerPlayer) player, launchPad, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}

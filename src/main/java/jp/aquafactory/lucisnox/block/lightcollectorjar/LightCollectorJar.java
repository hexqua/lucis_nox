package jp.aquafactory.lucisnox.block.lightcollectorjar;

import com.mojang.serialization.MapCodec;
import jp.aquafactory.lucisnox.registry.BlockEntityRegistry;
import jp.aquafactory.lucisnox.registry.ItemRegistry;
import jp.aquafactory.lucisnox.utility.MessageTools;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class LightCollectorJar extends BaseEntityBlock {
    public static final MapCodec<LightCollectorJar> CODEC = simpleCodec(LightCollectorJar::new);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.5D, 14.0D);

    public LightCollectorJar(Properties properties) {
        super(properties
                .strength(0.3f)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 3)
                .noOcclusion()
                .isSuffocating((state, getter, pos) -> false)
                .isViewBlocking((state, getter, pos) -> false));
    }

    public LightCollectorJar() {
        this(Properties.of());
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        // 瓶本体は JSON モデルで描画し、内部演出だけを BlockEntityRenderer に委ねる.
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                                        @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                                                 @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @NotNull VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 1.0f;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return BlockEntityRegistry.LIGHT_COLLECTOR_JAR.get().create(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                            @NotNull BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(
                type,
                BlockEntityRegistry.LIGHT_COLLECTOR_JAR.get(),
                LightCollectorJarBlockEntity::serverTick
        );
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack heldStack, @NotNull BlockState state, @NotNull Level level,
                                                       @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand,
                                                       @NotNull BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!(level.getBlockEntity(pos) instanceof LightCollectorJarBlockEntity blockEntity)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!heldStack.is(ItemRegistry.PHOSSHARD.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!blockEntity.canAcceptPhosshard(heldStack)) {
            if (!level.isClientSide) {
                MessageTools.sendActionBarError(player, "ui.lucisnox.light_collector_jar.cant_insert_more");
            }
            return ItemInteractionResult.CONSUME;
        }

        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        if (!blockEntity.startActiveGeneration(level.getGameTime())) {
            MessageTools.sendActionBarError(player, "ui.lucisnox.light_collector_jar.cant_insert_more");
            return ItemInteractionResult.CONSUME;
        }

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder builder) {
        var drops = new ArrayList<>(super.getDrops(state, builder));
        if (!(builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof LightCollectorJarBlockEntity blockEntity)) {
            return drops;
        }

        for (var stack : drops) {
            if (stack.is(ItemRegistry.LIGHT_COLLECTOR_JAR.get()) && blockEntity.getLevel() != null) {
                // 能動生産状態は設置中のみ有効で、アイテム化時には失わせる.
                blockEntity.saveToItemWithoutActiveState(stack, blockEntity.getLevel().registryAccess());
            }
        }

        return drops;
    }
}

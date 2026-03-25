package jp.aquafactory.lucisnox.block.lightcollectorjar;

import jp.aquafactory.lucisnox.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public final class LightCollectorJarBlockEntity extends BlockEntity {
    // 21600 = 1日フル.
    public static final int MAX_LICHT = 20000;
    public static final int MAX_GENERATION_PER_SECOND = 15;
    public static final String STORED_LICHT_TAG = "StoredLicht";

    private static final int GENERATION_INTERVAL_TICKS = 20;
    private static final int NIGHT_BASE_LIGHT = 4;
    private static final int DAY_BASE_LIGHT = 15;
    private static final int MOON_BONUS_CAP = 4;
    private static final long UNINITIALIZED_GAME_TIME = Long.MIN_VALUE;

    private int storedLicht;
    private long lastCollectionGameTime = UNINITIALIZED_GAME_TIME;

    public LightCollectorJarBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.LIGHT_COLLECTOR_JAR.get(), pos, blockState);
    }

    public int getStoredLicht() {
        return storedLicht;
    }

    public float getFillRatio() {
        return storedLicht / (float) MAX_LICHT;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null) {
            lastCollectionGameTime = level.getGameTime();
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (storedLicht > 0) {
            tag.putInt(STORED_LICHT_TAG, storedLicht);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        storedLicht = Mth.clamp(tag.getInt(STORED_LICHT_TAG), 0, MAX_LICHT);
        lastCollectionGameTime = UNINITIALIZED_GAME_TIME;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        var tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LightCollectorJarBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        var gameTime = level.getGameTime();
        if (blockEntity.lastCollectionGameTime == UNINITIALIZED_GAME_TIME) {
            blockEntity.lastCollectionGameTime = gameTime;
            return;
        }

        var elapsed = gameTime - blockEntity.lastCollectionGameTime;
        if (elapsed < GENERATION_INTERVAL_TICKS || blockEntity.storedLicht >= MAX_LICHT) {
            return;
        }

        var generationSteps = (int) (elapsed / GENERATION_INTERVAL_TICKS);
        var generationPerSecond = calculateGenerationPerSecond(level, pos);
        var generated = Math.min(MAX_LICHT - blockEntity.storedLicht, generationPerSecond * generationSteps);

        blockEntity.lastCollectionGameTime += (long) generationSteps * GENERATION_INTERVAL_TICKS;
        if (generated <= 0) {
            return;
        }

        blockEntity.storedLicht += generated;
        blockEntity.setChanged();
        blockEntity.syncToClient();
    }

    public static int calculateGenerationPerSecond(Level level, BlockPos pos) {
        var maxSkyLight = level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos);
        var timeBrightness = calculateCurrentTimeBrightness(level);
        var weatherPenalty = calculateWeatherPenalty(level);
        var naturalLight = Math.max(0, Math.min(maxSkyLight, timeBrightness) - weatherPenalty);
        var moonBonus = calculateMoonBonus(level, maxSkyLight);
        return Mth.clamp(naturalLight + moonBonus, 0, MAX_GENERATION_PER_SECOND);
    }

    private static int calculateMoonBonus(Level level, int maxSkyLight) {
        if (!level.isNight() || level.isRaining()) {
            return 0;
        }

        var rawMoonBonus = Math.abs(level.getMoonPhase() - MOON_BONUS_CAP);
        if (rawMoonBonus <= 0) {
            return 0;
        }

        var shadePenalty = Math.max(0, 15 - maxSkyLight);
        return Math.max(0, rawMoonBonus - shadePenalty);
    }

    private static int calculateCurrentTimeBrightness(Level level) {
        var sunAngle = level.getSunAngle(1.0f);
        var targetAngle = sunAngle < (float) Math.PI ? 0.0f : (float) (Math.PI * 2);
        sunAngle += (targetAngle - sunAngle) * 0.2f;
        var dayNightBlend = (Mth.cos(sunAngle) + 1.0f) * 0.5f;
        return Mth.clamp(Math.round(Mth.lerp(dayNightBlend, NIGHT_BASE_LIGHT, DAY_BASE_LIGHT)), NIGHT_BASE_LIGHT, DAY_BASE_LIGHT);
    }

    private static int calculateWeatherPenalty(Level level) {
        if (level.isThundering()) {
            return 5;
        }
        if (level.isRaining()) {
            return 3;
        }
        return 0;
    }

    private void syncToClient() {
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}

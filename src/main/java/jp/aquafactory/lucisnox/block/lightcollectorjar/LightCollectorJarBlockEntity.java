package jp.aquafactory.lucisnox.block.lightcollectorjar;

import jp.aquafactory.lucisnox.registry.BlockEntityRegistry;
import jp.aquafactory.lucisnox.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LightCollectorJarBlockEntity extends BlockEntity implements WorldlyContainer {
    // 21600 = 1日フル.
    public static final int MAX_LICHT = 20000;
    public static final int MAX_GENERATION_PER_SECOND = 15;
    public static final int ACTIVE_GENERATION_PER_SECOND = MAX_GENERATION_PER_SECOND * 2;
    public static final int ACTIVE_DURATION_TICKS = 20 * 60;
    public static final String STORED_LICHT_TAG = "StoredLicht";

    private static final String ACTIVE_GENERATION_UNTIL_TAG = "ActiveGenerationUntil";
    private static final int GENERATION_INTERVAL_TICKS = 20;
    private static final int NIGHT_BASE_LIGHT = 4;
    private static final int DAY_BASE_LIGHT = 15;
    private static final int MOON_BONUS_CAP = 4;
    private static final long UNINITIALIZED_GAME_TIME = Long.MIN_VALUE;
    private static final int[] INPUT_SLOTS = {0};

    private int storedLicht;
    private long lastCollectionGameTime = UNINITIALIZED_GAME_TIME;
    private long activeGenerationUntilGameTime;

    public LightCollectorJarBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.LIGHT_COLLECTOR_JAR.get(), pos, blockState);
    }

    public int getStoredLicht() {
        return storedLicht;
    }

    public float getFillRatio() {
        return storedLicht / (float) MAX_LICHT;
    }

    public boolean isActiveGeneration() {
        return level != null && isActiveGenerationAt(level.getGameTime());
    }

    public boolean isActiveGenerationAt(long gameTime) {
        return activeGenerationUntilGameTime > gameTime;
    }

    public long getActiveGenerationUntilGameTime() {
        return activeGenerationUntilGameTime;
    }

    public boolean canAcceptPhosshard(ItemStack stack) {
        return stack.is(ItemRegistry.PHOSSHARD.get()) && !isStorageFull() && !hasActivePhosshardLoaded();
    }

    public boolean startActiveGeneration(long gameTime) {
        if (isStorageFull() || hasActivePhosshardLoaded()) {
            return false;
        }

        activeGenerationUntilGameTime = gameTime + ACTIVE_DURATION_TICKS;
        markUpdated();
        return true;
    }

    public void saveToItemWithoutActiveState(ItemStack stack, HolderLookup.Provider registries) {
        saveToItem(stack, registries);

        var customData = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
        if (customData.isEmpty()) {
            return;
        }

        var tag = customData.copyTag();
        tag.remove(ACTIVE_GENERATION_UNTIL_TAG);
        if (!tag.contains(STORED_LICHT_TAG)) {
            stack.remove(DataComponents.BLOCK_ENTITY_DATA);
            return;
        }

        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
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
        if (activeGenerationUntilGameTime > 0L) {
            tag.putLong(ACTIVE_GENERATION_UNTIL_TAG, activeGenerationUntilGameTime);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        storedLicht = Mth.clamp(tag.getInt(STORED_LICHT_TAG), 0, MAX_LICHT);
        activeGenerationUntilGameTime = Math.max(0L, tag.getLong(ACTIVE_GENERATION_UNTIL_TAG));
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

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return !hasActivePhosshardLoaded();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        if (slot != 0 || !hasActivePhosshardLoaded()) {
            return ItemStack.EMPTY;
        }

        return ItemRegistry.PHOSSHARD.toStack();
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        if (slot != 0 || level == null || !canPlaceItem(slot, stack)) {
            return;
        }

        startActiveGeneration(level.getGameTime());
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (level == null) {
            return false;
        }

        return level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return slot == 0 && canAcceptPhosshard(stack);
    }

    @Override
    public boolean canTakeItem(@NotNull net.minecraft.world.Container target, int slot, @NotNull ItemStack stack) {
        return false;
    }

    @Override
    public void clearContent() {
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return INPUT_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, @Nullable Direction side) {
        return slot == 0 && side != null && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack stack, @NotNull Direction side) {
        return false;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LightCollectorJarBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        if (blockEntity.lastCollectionGameTime == UNINITIALIZED_GAME_TIME) {
            blockEntity.lastCollectionGameTime = level.getGameTime();
            return;
        }

        var gameTime = level.getGameTime();
        var updated = blockEntity.expireActiveGenerationIfNeeded(gameTime);

        while ((blockEntity.lastCollectionGameTime + GENERATION_INTERVAL_TICKS) <= gameTime) {
            blockEntity.lastCollectionGameTime += GENERATION_INTERVAL_TICKS;
            if (blockEntity.storedLicht >= MAX_LICHT) {
                continue;
            }

            var generationPerSecond = blockEntity.resolveGenerationPerSecond(level, pos, blockEntity.lastCollectionGameTime - 1L);
            if (generationPerSecond <= 0) {
                continue;
            }

            blockEntity.storedLicht = Math.min(MAX_LICHT, blockEntity.storedLicht + generationPerSecond);
            updated = true;
        }

        if (updated) {
            blockEntity.markUpdated();
        }
    }

    public static int calculateGenerationPerSecond(Level level, BlockPos pos) {
        var maxSkyLight = level.getBrightness(LightLayer.SKY, pos);
        var timeBrightness = calculateCurrentTimeBrightness(level);
        var weatherPenalty = calculateWeatherPenalty(level);
        var naturalLight = Math.max(0, Math.min(maxSkyLight, timeBrightness) - weatherPenalty);
        var moonBonus = calculateMoonBonus(level, maxSkyLight);
        return Mth.clamp(naturalLight + moonBonus, 0, MAX_GENERATION_PER_SECOND);
    }

    private int resolveGenerationPerSecond(Level level, BlockPos pos, long gameTime) {
        if (isActiveGenerationAt(gameTime)) {
            return ACTIVE_GENERATION_PER_SECOND;
        }

        return calculateGenerationPerSecond(level, pos);
    }

    private boolean expireActiveGenerationIfNeeded(long gameTime) {
        if (!hasActivePhosshardLoaded() || activeGenerationUntilGameTime > gameTime) {
            return false;
        }

        activeGenerationUntilGameTime = 0L;
        return true;
    }

    private boolean hasActivePhosshardLoaded() {
        return activeGenerationUntilGameTime > 0L;
    }

    private boolean isStorageFull() {
        return storedLicht >= MAX_LICHT;
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

    private void markUpdated() {
        setChanged();
        syncToClient();
    }

    private void syncToClient() {
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}

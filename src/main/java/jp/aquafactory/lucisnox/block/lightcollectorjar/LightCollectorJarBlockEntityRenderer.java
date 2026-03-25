package jp.aquafactory.lucisnox.block.lightcollectorjar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import jp.aquafactory.lucisnox.renderer.LucisNoxRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class LightCollectorJarBlockEntityRenderer implements BlockEntityRenderer<LightCollectorJarBlockEntity> {
    private static final float INNER_MIN_X = 4.0f / 16.0f;
    private static final float INNER_MAX_X = 12.0f / 16.0f;
    private static final float INNER_MIN_Z = 4.0f / 16.0f;
    private static final float INNER_MAX_Z = 12.0f / 16.0f;
    private static final float INNER_MIN_Y = 1.0f / 16.0f;
    private static final float INNER_MAX_Y = 11.5f / 16.0f;
    private static final float CENTER_XZ = 0.5f;
    private static final float CENTER_Y = 6.5f / 16.0f;
    private static final float MIN_OUTER_CORE_SIZE = 1.9f / 16.0f;
    private static final float MAX_OUTER_CORE_SIZE = 3.5f / 16.0f;
    private static final double MAX_RENDER_DISTANCE = 48.0;
    private static final double MAX_RENDER_DISTANCE_SQR = MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE;

    private static final RenderType INNER_CORE_RENDER_TYPE = LucisNoxRenderTypes.color("light_collector_jar_inner_core");
    private static final RenderType OUTER_CORE_RENDER_TYPE = LucisNoxRenderTypes.translucentColorCull("light_collector_jar_outer_core");
    private static final RenderType FILL_RENDER_TYPE = LucisNoxRenderTypes.translucentColor("light_collector_jar_fill");

    public LightCollectorJarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull LightCollectorJarBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        var level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        var cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        var center = Vec3.atCenterOf(blockEntity.getBlockPos()).add(0.0, CENTER_Y - 0.5, 0.0);
        if (cameraPos.distanceToSqr(center) > MAX_RENDER_DISTANCE_SQR) {
            return;
        }

        var time = level.getGameTime() + partialTick + (blockEntity.getBlockPos().asLong() & 31L);
        var colorFade = 0.5f + 0.5f * Mth.sin(time * 0.035f);
        var generationRatio = LightCollectorJarBlockEntity.calculateGenerationPerSecond(level, blockEntity.getBlockPos())
                / (float) LightCollectorJarBlockEntity.MAX_GENERATION_PER_SECOND;
        var fillRatio = blockEntity.getFillRatio();

        if (fillRatio > 0.0f) {
            var fillMaxY = Mth.lerp(fillRatio, INNER_MIN_Y, INNER_MAX_Y);
            var fillRed = mixChannel(colorFade, 255, 132);
            var fillGreen = mixChannel(colorFade, 212, 188);
            var fillBlue = mixChannel(colorFade, 120, 255);

            drawBox(
                    poseStack,
                    buffer.getBuffer(FILL_RENDER_TYPE),
                    INNER_MIN_X,
                    INNER_MIN_Y,
                    INNER_MIN_Z,
                    INNER_MAX_X,
                    fillMaxY,
                    INNER_MAX_Z,
                    fillRed,
                    fillGreen,
                    fillBlue,
                    88
            );
        }

        if (generationRatio <= 0.0f) {
            return;
        }

        var coreRed = mixChannel(colorFade, 255, 72);
        var coreGreen = mixChannel(colorFade, 168, 216);
        var coreBlue = mixChannel(colorFade, 20, 255);
        var outerCoreSize = Mth.lerp(generationRatio, MIN_OUTER_CORE_SIZE, MAX_OUTER_CORE_SIZE);
        var innerCoreSize = outerCoreSize * 0.52f;

        poseStack.pushPose();
        poseStack.translate(CENTER_XZ, CENTER_Y, CENTER_XZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(time * 1.10f));
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 1.85f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(time * 1.35f));
        drawCenteredCube(poseStack, buffer.getBuffer(OUTER_CORE_RENDER_TYPE), outerCoreSize, coreRed, coreGreen, coreBlue, 255, true);
        drawCenteredCube(poseStack, buffer.getBuffer(INNER_CORE_RENDER_TYPE), innerCoreSize, 255, 255, 255, 255, false);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(@NotNull LightCollectorJarBlockEntity blockEntity, @NotNull Vec3 cameraPos) {
        var center = Vec3.atCenterOf(blockEntity.getBlockPos()).add(0.0, CENTER_Y - 0.5, 0.0);
        return cameraPos.distanceToSqr(center) <= MAX_RENDER_DISTANCE_SQR;
    }

    private static void drawCenteredCube(PoseStack poseStack, VertexConsumer consumer, float size,
                                         int red, int green, int blue, int alpha, boolean inwardFaces) {
        var half = size * 0.5f;
        drawBox(poseStack, consumer, -half, -half, -half, half, half, half, red, green, blue, alpha, inwardFaces);
    }

    private static void drawBox(PoseStack poseStack, VertexConsumer consumer,
                                float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                                int red, int green, int blue, int alpha) {
        drawBox(poseStack, consumer, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha, false);
    }

    private static void drawBox(PoseStack poseStack, VertexConsumer consumer,
                                float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                                int red, int green, int blue, int alpha, boolean inwardFaces) {
        var pose = poseStack.last().pose();

        quad(pose, consumer, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha, inwardFaces);
        quad(pose, consumer, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha, inwardFaces);
        quad(pose, consumer, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha, inwardFaces);
        quad(pose, consumer, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha, inwardFaces);
        quad(pose, consumer, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, red, green, blue, alpha, inwardFaces);
        quad(pose, consumer, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha, inwardFaces);
    }

    private static void quad(Matrix4f pose, VertexConsumer consumer,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             int red, int green, int blue, int alpha, boolean inwardFaces) {
        if (inwardFaces) {
            vertex(consumer, pose, x4, y4, z4, red, green, blue, alpha);
            vertex(consumer, pose, x3, y3, z3, red, green, blue, alpha);
            vertex(consumer, pose, x2, y2, z2, red, green, blue, alpha);
            vertex(consumer, pose, x1, y1, z1, red, green, blue, alpha);
            return;
        }

        vertex(consumer, pose, x1, y1, z1, red, green, blue, alpha);
        vertex(consumer, pose, x2, y2, z2, red, green, blue, alpha);
        vertex(consumer, pose, x3, y3, z3, red, green, blue, alpha);
        vertex(consumer, pose, x4, y4, z4, red, green, blue, alpha);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z,
                               int red, int green, int blue, int alpha) {
        consumer.addVertex(pose, x, y, z).setColor(red, green, blue, alpha);
    }

    private static int mixChannel(float delta, int sunChannel, int moonChannel) {
        return Mth.clamp((int) Mth.lerp(delta, sunChannel, moonChannel), 0, 255);
    }
}

package jp.aquafactory.lucisnox.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public final class LucisNoxRenderTypes extends RenderStateShard {
    private LucisNoxRenderTypes(String name, Runnable setupState, Runnable clearState) {
        super(name, setupState, clearState);
    }

    public static RenderType color(String renderTypeName) {
        return RenderType.create(
                renderTypeName,
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS,
                256,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER)
                        .setTextureState(NO_TEXTURE)
                        .setTransparencyState(NO_TRANSPARENCY)
                        .setLightmapState(NO_LIGHTMAP)
                        .setCullState(CULL)
                        .createCompositeState(false)
        );
    }

    public static RenderType translucentColor(String renderTypeName) {
        return RenderType.create(
                renderTypeName,
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER)
                        .setTextureState(NO_TEXTURE)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setWriteMaskState(COLOR_WRITE)
                        .setLightmapState(NO_LIGHTMAP)
                        .setCullState(NO_CULL)
                        .createCompositeState(false)
        );
    }

    public static RenderType translucentColorCull(String renderTypeName) {
        return RenderType.create(
                renderTypeName,
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER)
                        .setTextureState(NO_TEXTURE)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setWriteMaskState(COLOR_WRITE)
                        .setLightmapState(NO_LIGHTMAP)
                        .setCullState(CULL)
                        .createCompositeState(false)
        );
    }
}

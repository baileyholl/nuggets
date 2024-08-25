package src.main.java.com.hollingsworth.nuggets.client.rendering;

import src.main.java.com.hollingsworth.nuggets.common.Color;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.List;

public class RenderHelpers {

    private static final RenderType TRANSLUCENT = RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);

    public static void drawItemAsIcon(ItemStack itemStack, GuiGraphics graphics, int positionX, int positionY, int size, boolean renderTransparent) {
        renderFakeItemTransparent(graphics.pose(), itemStack, positionX, positionY, size, 0, renderTransparent,150);
    }

    public static void renderFakeItemTransparent(PoseStack poseStack, ItemStack stack, int x, int y, int scale, int alpha, boolean transparent, int zIndex) {
        if (stack.isEmpty()) {
            return;
        }

        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

        BakedModel model = renderer.getModel(stack, null, Minecraft.getInstance().player, 0);
        renderItemModel(poseStack, stack, x, y, scale, alpha, model, renderer, transparent, zIndex);
    }

    private static final Matrix4f SCALE_INVERT_Y = new Matrix4f().scaling(1F, -1F, 1F);

    public static void renderItemModel(PoseStack poseStack, ItemStack stack, int x, int y, int scale, int alpha, BakedModel model, ItemRenderer renderer, boolean transparent, int zIndex) {
        poseStack.pushPose();
        poseStack.translate(x + 8F, y + 8F, zIndex);
        poseStack.mulPose(SCALE_INVERT_Y);
        poseStack.scale(scale, scale, scale);

        boolean flatLight = !model.usesBlockLight();
        if (flatLight) {
            Lighting.setupForFlatItems();
        }

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        renderer.render(
                stack,
                ItemDisplayContext.GUI,
                false,
                poseStack,
                transparent ? transparentBuffer(buffer) : buffer,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                model
        );
        buffer.endBatch();

        RenderSystem.enableDepthTest();

        if (flatLight) {
            Lighting.setupFor3DItems();
        }

        poseStack.popPose();
    }


    private static MultiBufferSource transparentBuffer(MultiBufferSource buffer) {
        return renderType -> new TintedVertexConsumer(buffer.getBuffer(TRANSLUCENT), 1.0f, 1.0f, 1.0f, 0.25f);
    }

    /*
     * Adapted from Eidolon, Elucent
     */
    public static void colorBlit(PoseStack mStack, int x, int y, int uOffset, int vOffset, int width, int height, int textureWidth, int textureHeight, Color color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Matrix4f matrix = mStack.last().pose();
        int maxX = x + width, maxY = y + height;
        float minU = (float) uOffset / textureWidth, minV = (float) vOffset / textureHeight;
        float maxU = minU + (float) width / textureWidth, maxV = minV + (float) height / textureHeight;
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue(), alpha = color.getAlpha();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix, (float) x, (float) maxY, 0).setUv(minU, maxV).setColor(r, g, b, alpha);
        bufferbuilder.addVertex(matrix, (float) maxX, (float) maxY, 0).setUv(maxU, maxV).setColor(r, g, b, alpha);
        bufferbuilder.addVertex(matrix, (float) maxX, (float) y, 0).setUv(maxU, minV).setColor(r, g, b, alpha);
        bufferbuilder.addVertex(matrix, (float) x, (float) y, 0).setUv(minU, minV).setColor(r, g, b, alpha);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void renderTooltipInternal(GuiGraphics graphics, List<ClientTooltipComponent> pClientTooltipComponents, int pMouseX, int pMouseY, Screen parentScreen) {
        var font = parentScreen.getMinecraft().font;
        var parentWidth = parentScreen.width;
        var parentHeight = parentScreen.height;
        if (!pClientTooltipComponents.isEmpty()) {
            PoseStack pPoseStack = graphics.pose();
            net.neoforged.neoforge.client.event.RenderTooltipEvent.Pre preEvent = net.neoforged.neoforge.client.ClientHooks.onRenderTooltipPre(ItemStack.EMPTY, graphics, pMouseX, pMouseY, parentWidth, parentHeight, pClientTooltipComponents, font, DefaultTooltipPositioner.INSTANCE);
            if (preEvent.isCanceled()) return;
            int i = 0;
            int j = pClientTooltipComponents.size() == 1 ? -2 : 0;

            for (ClientTooltipComponent clienttooltipcomponent : pClientTooltipComponents) {
                int k = clienttooltipcomponent.getWidth(preEvent.getFont());
                if (k > i) {
                    i = k;
                }

                j += clienttooltipcomponent.getHeight();
            }

            int j2 = preEvent.getX() + 12;
            int k2 = preEvent.getY() - 12;
            if (j2 + i > parentWidth) {
                j2 -= 28 + i;
            }

            if (k2 + j + 6 > parentHeight) {
                k2 = parentHeight - j - 6;
            }
            pPoseStack.pushPose();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            Matrix4f matrix4f = pPoseStack.last().pose();
            net.neoforged.neoforge.client.event.RenderTooltipEvent.Color colorEvent = net.neoforged.neoforge.client.ClientHooks.onRenderTooltipColor(ItemStack.EMPTY, graphics, j2, k2, preEvent.getFont(), pClientTooltipComponents);

            graphics.fillGradient(j2 - 3, k2 - 4, j2 + i + 3, k2 - 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundStart());
            graphics.fillGradient(j2 - 3, k2 + j + 3, j2 + i + 3, k2 + j + 4, 400, colorEvent.getBackgroundEnd(), colorEvent.getBackgroundEnd());
            graphics.fillGradient(j2 - 3, k2 - 3, j2 + i + 3, k2 + j + 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd());
            graphics.fillGradient(j2 - 4, k2 - 3, j2 - 3, k2 + j + 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd());
            graphics.fillGradient(j2 + i + 3, k2 - 3, j2 + i + 4, k2 + j + 3, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd());
            graphics.fillGradient(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + j + 3 - 1, 400, colorEvent.getBorderStart(), colorEvent.getBorderEnd());
            graphics.fillGradient(j2 + i + 2, k2 - 3 + 1, j2 + i + 3, k2 + j + 3 - 1, 400, colorEvent.getBorderStart(), colorEvent.getBorderEnd());
            graphics.fillGradient(j2 - 3, k2 - 3, j2 + i + 3, k2 - 3 + 1, 400, colorEvent.getBorderStart(), colorEvent.getBorderStart());
            graphics.fillGradient(j2 - 3, k2 + j + 2, j2 + i + 3, k2 + j + 3, 400, colorEvent.getBorderEnd(), colorEvent.getBorderEnd());

            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            MultiBufferSource.BufferSource multibuffersource$buffersource = graphics.bufferSource();
            pPoseStack.translate(0.0D, 0.0D, 400.0D);
            int l1 = k2;

            for (int i2 = 0; i2 < pClientTooltipComponents.size(); ++i2) {
                ClientTooltipComponent clienttooltipcomponent1 = pClientTooltipComponents.get(i2);
                clienttooltipcomponent1.renderText(preEvent.getFont(), j2, l1, matrix4f, multibuffersource$buffersource);
                l1 += clienttooltipcomponent1.getHeight() + (i2 == 0 ? 2 : 0);
            }
            l1 = k2;

            pPoseStack.translate(0, 0, 600);
            for (int l2 = 0; l2 < pClientTooltipComponents.size(); ++l2) {
                ClientTooltipComponent clienttooltipcomponent2 = pClientTooltipComponents.get(l2);
                clienttooltipcomponent2.renderImage(preEvent.getFont(), j2, l1, graphics);
                l1 += clienttooltipcomponent2.getHeight() + (l2 == 0 ? 2 : 0);
            }
            pPoseStack.popPose();

//            this.itemRenderer.blitOffset = f;
        }
    }
}

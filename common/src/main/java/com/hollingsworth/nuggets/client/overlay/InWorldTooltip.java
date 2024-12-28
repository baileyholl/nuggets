package com.hollingsworth.nuggets.client.overlay;

import com.hollingsworth.nuggets.client.Color;
import com.hollingsworth.nuggets.client.gui.GuiHelpers;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InWorldTooltip {
    public static MultiBufferSource.BufferSource renderType = MultiBufferSource.immediate(new ByteBufferBuilder(1536));

    public static int hoverTicks = 0;
    public static Object lastHovered = null;

    public static Map<EntityType<?>, List<ITooltipConsumer>> ENTITY_CALLBACKS = new ConcurrentHashMap<>();

    public static void registerEntityCallback(EntityType<?> type, ITooltipConsumer callback) {
        ENTITY_CALLBACKS.computeIfAbsent(type, k -> new ArrayList<>()).add(callback);
    }

    public static void renderOverlay(GuiGraphics graphics, DeltaTracker tracker) {
        PoseStack poseStack = graphics.pose();
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;
        int xOffset = 20;
        int yOffset = 0;
        HitResult objectMouseOver = mc.hitResult;
        List<Component> tooltip = new ArrayList<>();
        Object hovering = null;
        if(objectMouseOver instanceof BlockHitResult hitResult){
            hovering = hitResult.getBlockPos();
            if(mc.level.getBlockEntity(hitResult.getBlockPos()) instanceof IWorldTooltipProvider iWorldTooltipProvider){
                iWorldTooltipProvider.getTooltip(tooltip);
            }
        }else if(objectMouseOver instanceof EntityHitResult result){
            if (result.getEntity() instanceof IWorldTooltipProvider iWorldTooltipProvider) {
                iWorldTooltipProvider.getTooltip(tooltip);
            }
            var consumers = ENTITY_CALLBACKS.get(result.getEntity().getType());
            if (consumers != null) {
                for(ITooltipConsumer consumer : consumers) {
                    consumer.accept(result, tooltip);
                }
            }
            hovering = result.getEntity();
        }

        if (hovering == null || (lastHovered != null && !lastHovered.equals(hovering))) {
            lastHovered = null;
            hoverTicks = 0;
        }

        if (lastHovered == null || lastHovered.equals(hovering))
            hoverTicks++;
        else
            hoverTicks = 0;
        lastHovered = hovering;


        if (tooltip.isEmpty()) {
            return;
        }
        poseStack.pushPose();

        int tooltipTextWidth = 0;
        for (FormattedText textLine : tooltip) {
            int textLineWidth = mc.font.width(textLine);
            if (textLineWidth > tooltipTextWidth)
                tooltipTextWidth = textLineWidth;
        }

        int tooltipHeight = 8;
        if (tooltip.size() > 1) {
            tooltipHeight += 2; // gap between title lines and next lines
            tooltipHeight += (tooltip.size() - 1) * 10;
        }
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int posX = width / 2 + xOffset;
        int posY = height / 2 + yOffset;

        posX = Math.min(posX, width - tooltipTextWidth - 20);
        posY = Math.min(posY, height - tooltipHeight - 20);

        float fade = Mth.clamp((hoverTicks + tracker.getGameTimeDeltaTicks()) / 12f, 0, 1);
        Color colorBackground = VANILLA_TOOLTIP_BACKGROUND.scaleAlpha(.75f);
        Color colorBorderTop = VANILLA_TOOLTIP_BORDER_1;
        Color colorBorderBot = VANILLA_TOOLTIP_BORDER_2;

        if (fade < 1) {
            poseStack.translate((1 - fade) * Math.signum(xOffset + .5f) * 4, 0, 0);
            colorBackground.scaleAlpha(fade);
            colorBorderTop.scaleAlpha(fade);
            colorBorderBot.scaleAlpha(fade);
        }
        drawHoveringText(ItemStack.EMPTY, graphics, tooltip,  posX, posY, width, height, -1, colorBackground.getRGB(),
                colorBorderTop.getRGB(), colorBorderBot.getRGB(), mc.font);
        poseStack.popPose();
    }
    public static final Color VANILLA_TOOLTIP_BORDER_1 = new Color(0x50_5000ff, true);
    public static final Color VANILLA_TOOLTIP_BORDER_2 = new Color(0x50_28007f, true);
    public static final Color VANILLA_TOOLTIP_BACKGROUND =  new Color(0xf0_100010, true);


    public static void drawHoveringText(@NotNull final ItemStack stack, GuiGraphics graphics,
                                        List<? extends FormattedText> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight,
                                        int maxTextWidth, int backgroundColor, int borderColorStart, int borderColorEnd, Font font) {
        if (textLines.isEmpty())
            return;

        PoseStack pStack = graphics.pose();
        List<ClientTooltipComponent> list = GuiHelpers.gatherTooltipComponents(textLines, stack.getTooltipImage(), mouseX, screenWidth, screenHeight, font);

        RenderSystem.disableDepthTest();
        int tooltipTextWidth = 0;

        for (FormattedText textLine : textLines) {
            int textLineWidth = font.width(textLine);
            if (textLineWidth > tooltipTextWidth)
                tooltipTextWidth = textLineWidth;
        }

        boolean needsWrap = false;

        int titleLinesCount = 1;
        int tooltipX = mouseX + 12;
        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
            tooltipX = mouseX - 16 - tooltipTextWidth;
            if (tooltipX < 4) // if the tooltip doesn't fit on the screen
            {
                if (mouseX > screenWidth / 2)
                    tooltipTextWidth = mouseX - 12 - 8;
                else
                    tooltipTextWidth = screenWidth - 16 - mouseX;
                needsWrap = true;
            }
        }

        if (needsWrap) {
            int wrappedTooltipWidth = 0;
            List<FormattedText> wrappedTextLines = new ArrayList<>();
            for (int i = 0; i < textLines.size(); i++) {
                FormattedText textLine = textLines.get(i);
                List<FormattedText> wrappedLine = font.getSplitter()
                        .splitLines(textLine, tooltipTextWidth, Style.EMPTY);
                if (i == 0)
                    titleLinesCount = wrappedLine.size();

                for (FormattedText line : wrappedLine) {
                    int lineWidth = font.width(line);
                    if (lineWidth > wrappedTooltipWidth)
                        wrappedTooltipWidth = lineWidth;
                    wrappedTextLines.add(line);
                }
            }
            tooltipTextWidth = wrappedTooltipWidth;
            textLines = wrappedTextLines;

            if (mouseX > screenWidth / 2)
                tooltipX = mouseX - 16 - tooltipTextWidth;
            else
                tooltipX = mouseX + 12;
        }

        int tooltipY = mouseY - 12;
        int tooltipHeight = 8;

        if (textLines.size() > 1) {
            tooltipHeight += (textLines.size() - 1) * 10;
            if (textLines.size() > titleLinesCount)
                tooltipHeight += 2; // gap between title lines and next lines
        }

        if (tooltipY < 4)
            tooltipY = 4;
        else if (tooltipY + tooltipHeight + 4 > screenHeight)
            tooltipY = screenHeight - tooltipHeight - 4;

        final int zLevel = 400;


        pStack.pushPose();
        Matrix4f mat = pStack.last()
                .pose();
        graphics.fillGradient(tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3,
                tooltipY - 3, backgroundColor, backgroundColor);
        graphics.fillGradient(tooltipX - 3, tooltipY + tooltipHeight + 3,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
        graphics.fillGradient(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
                tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
        graphics.fillGradient(tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3,
                backgroundColor, backgroundColor);
        graphics.fillGradient(tooltipX + tooltipTextWidth + 3, tooltipY - 3,
                tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
        graphics.fillGradient(tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1,
                tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
        graphics.fillGradient(tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
        graphics.fillGradient(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
                tooltipY - 3 + 1, borderColorStart, borderColorStart);
        graphics.fillGradient(tooltipX - 3, tooltipY + tooltipHeight + 2,
                tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);
        pStack.translate(0.0D, 0.0D, zLevel);
        for (int lineNumber = 0; lineNumber < list.size(); ++lineNumber) {
            ClientTooltipComponent line = list.get(lineNumber);

            if (line != null)
                line.renderText(font, tooltipX, tooltipY, mat, renderType);

            if (lineNumber + 1 == titleLinesCount)
                tooltipY += 2;

            tooltipY += 10;
        }
        renderType.endBatch();
        pStack.popPose();

        RenderSystem.enableDepthTest();
    }
}

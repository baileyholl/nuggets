package com.hollingsworth.nuggets.client.gui;

import com.hollingsworth.nuggets.mixin.ScreenAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class BaseScreen extends Screen {

    public int maxScale;
    public float scaleFactor;
    public int bookLeft;
    public int bookTop;
    public int bookRight;
    public int bookBottom;

    public int fullWidth;
    public int fullHeight;

    public ResourceLocation background;

    public BaseScreen(Component titleIn, int fullWidth, int fullHeight, ResourceLocation background) {
        super(titleIn);
        this.fullHeight = fullHeight;
        this.fullWidth = fullWidth;
        this.background = background;
    }

    @Override
    public void init() {
        super.init();
        this.maxScale = this.getMaxAllowedScale();
        this.scaleFactor = 1.0F;
        bookLeft = width / 2 - fullWidth / 2;
        bookTop = height / 2 - fullHeight / 2;
        bookRight = width / 2 + fullWidth / 2;
        bookBottom = height / 2 + fullHeight / 2;
    }

    public void drawTooltip(GuiGraphics stack, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        collectTooltips(stack, mouseX, mouseY, tooltip);
        if (!tooltip.isEmpty()) {
            stack.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    public void collectTooltips(GuiGraphics stack, int mouseX, int mouseY, List<Component> tooltip){
        for(Renderable renderable : renderablesList()){
            if(renderable instanceof AbstractWidget widget && renderable instanceof ITooltipRenderer tooltipProvider){
                if(GuiHelpers.isMouseInRelativeRange(mouseX, mouseY, widget) && widget.visible){
                    tooltipProvider.gatherTooltips(stack, mouseX, mouseY, tooltip);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener.mouseClicked(pMouseX, pMouseY, pButton)) {
                if(guieventlistener instanceof NestedWidgets nestedWidgets){
                    for(AbstractWidget extra : nestedWidgets.getExtras()){
                        extra.mouseClicked(pMouseX, pMouseY, pButton);
                    }

                }
                this.setFocused(guieventlistener);
                if (pButton == 0) {
                    this.setDragging(true);
                }

                return true;
            }
        }

        return false;
    }

    public void drawForegroundElements(int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void drawScreenAfterScale(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics, mouseX, mouseY, partialTicks);
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(bookLeft, bookTop, 0);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        drawBackgroundElements(graphics, mouseX, mouseY, partialTicks);
        drawForegroundElements(mouseX, mouseY, partialTicks);
        poseStack.popPose();
        for (Renderable renderable : this.renderablesList()) {
            renderable.render(graphics, mouseX, mouseY, partialTicks);
        }
        drawTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        if (scaleFactor != 1) {
            matrixStack.scale(scaleFactor, scaleFactor, scaleFactor);
            mouseX /= scaleFactor;
            mouseY /= scaleFactor;
        }
        drawScreenAfterScale(graphics, mouseX, mouseY, partialTicks);
        matrixStack.popPose();
    }

    public void drawBackgroundElements(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.blit(background, 0, 0, 0, 0, fullWidth, fullHeight, fullWidth, fullHeight);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int getMaxAllowedScale() {
        return this.minecraft.getWindow().calculateScale(0, this.minecraft.isEnforceUnicode());
    }

    @Override
    protected void renderBlurredBackground(float pPartialTick) {

    }

    public List<Renderable> renderablesList() {
        return ((ScreenAccessor)this).getRenderables();
    }

    @Override
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T pWidget) {
        T t = super.addRenderableWidget(pWidget);
        if(pWidget instanceof NestedWidgets nestedWidgets){
            for(AbstractWidget widget : nestedWidgets.getExtras()){
                super.addRenderableWidget(widget);
            }
        }
        return t;
    }

    @Override
    protected void removeWidget(GuiEventListener pListener) {
        super.removeWidget(pListener);
        if(pListener instanceof NestedWidgets nestedWidgets){
            for(AbstractWidget renderable : nestedWidgets.getExtras()){
                removeWidget(renderable);
            }
        }
    }
}

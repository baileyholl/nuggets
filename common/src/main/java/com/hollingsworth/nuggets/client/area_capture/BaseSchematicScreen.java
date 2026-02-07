package com.hollingsworth.nuggets.client.area_capture;

import com.hollingsworth.nuggets.client.gui.GuiHelpers;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class BaseSchematicScreen extends Screen {
    public final String holdToFocus = "nuggets.gui.toolmenu.focusKey";
    public String modId;
    public KeyMapping focusKey;
    public List<ToolType> tools = new ArrayList<>();

    public boolean focused;
    protected float yOffset;
    protected int selection;
    protected boolean initialized;

    protected int w;
    protected int h;


    protected BaseSchematicScreen(String modId, KeyMapping focusKey, Component pTitle) {
        super(pTitle);
        this.minecraft = Minecraft.getInstance();
        this.modId = modId;
        this.focusKey = focusKey;
        focused = false;
        yOffset = 0;
        selection = 0;
        initialized = false;
        h = 34;
    }

    public ToolType getSelectedElement() {
        return tools.get(selection);
    }

    public void cycle(int direction) {
        selection += (direction < 0) ? 1 : -1;
        selection = (selection + tools.size()) % tools.size();
    }

    private void draw(GuiGraphics graphics, float partialTicks) {
        final int cellW = 50;

        final int padX = 14;
        final int minW = 260;

        int count = tools.size();
        int rowW = count * cellW;

        w = Math.max(rowW + padX * 2, minW);

        PoseStack matrixStack = graphics.pose();
        Window mainWindow = minecraft.getWindow();
        if (!initialized)
            init(minecraft, mainWindow.getGuiScaledWidth(), mainWindow.getGuiScaledHeight());

        int panelX = (mainWindow.getGuiScaledWidth() - w) / 2;
        int y = mainWindow.getGuiScaledHeight() - h - 34;

        matrixStack.pushPose();
        matrixStack.translate(0, -yOffset, focused ? 100 : 0);

        graphics.blit(ResourceLocation.fromNamespaceAndPath(modId, "textures/gui/hud_background.png"),
                panelX - 15 + padX, y, 0, 0, w, h, 16, 16);

        float toolTipAlpha = yOffset / 10;
        List<Component> toolTip = tools.get(selection).getDescription();

        if (toolTipAlpha > 0.25f) {
            graphics.blit(ResourceLocation.fromNamespaceAndPath(modId, "textures/gui/hud_background.png"),
                    panelX - 15 + padX, y + 32, 0, 0, w, h, 16, 16);
            if (!toolTip.isEmpty())
                GuiHelpers.drawOutlinedText(minecraft.font, graphics, toolTip.get(0), panelX + 10, y + 38);
            if (toolTip.size() > 1)
                GuiHelpers.drawOutlinedText(minecraft.font, graphics, toolTip.get(1), panelX + 10, y + 50);
        }

        int width = minecraft.getWindow().getGuiScaledWidth();
        String keyName = focusKey.getTranslatedKeyMessage().getString();
        if (!focused)
            GuiHelpers.drawCenteredOutlinedText(minecraft.font, graphics, Component.translatable(holdToFocus, keyName),
                    width / 2, y - 10);
        else
            GuiHelpers.drawCenteredOutlinedText(minecraft.font, graphics, Component.translatable("nuggets.scroll"),
                    width / 2, y - 10);

        int startX = panelX + Math.max(padX, (w - rowW) / 2);

        for (int i = 0; i < count; i++) {
            matrixStack.pushPose();

            int cellX = startX + i * cellW;

            if (i == selection) {
                matrixStack.translate(0, -10, 0);
                GuiHelpers.drawCenteredOutlinedText(
                        minecraft.font, graphics,
                        tools.get(i).getDisplayName(),
                        cellX + 26, y + 28
                );
            }

            ResourceLocation icon = tools.get(i).getIcon();
            graphics.blit(icon, cellX + 16, y + 11, 0, 0, 16, 16, 16, 16);

            matrixStack.popPose();
        }

        matrixStack.popPose();
    }

    public void update() {
        if (focused)
            yOffset += (10 - yOffset) * .1f;
        else
            yOffset *= .9f;
    }

    public boolean scroll(double delta) {
        if (focused) {
            cycle((int) delta);
            return true;
        } else if (hasControlDown()) {
            return tools.get(selection)
                    .handleMouseWheel(delta);
        }
        return false;
    }

    public void renderPassive(GuiGraphics graphics, float partialTicks) {
        draw(graphics, partialTicks);
    }

    @Override
    public void onClose() {

    }

    @Override
    protected void init() {
        super.init();
        initialized = true;
    }

}

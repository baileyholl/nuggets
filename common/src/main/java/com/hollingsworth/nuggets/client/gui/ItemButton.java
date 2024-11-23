package com.hollingsworth.nuggets.client.gui;

import com.hollingsworth.nuggets.client.NuggetClientData;
import com.hollingsworth.nuggets.client.rendering.RenderHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemButton extends BaseButton{
    public Ingredient ingredient;
    public Screen screen;
    public int scale = 16;
    List<Component> tooltips = new ArrayList<>();

    public ItemButton(int x, int y, int w, int h, @NotNull Component text, OnPress onPress, Ingredient ingredient, Screen screen) {
        super(x, y, w, h, text, onPress);
        this.ingredient = ingredient;
        this.screen = screen;
    }

    public ItemButton(int x, int y, int w, int h, @NotNull Component text, OnPress onPress, ItemStack stack, Screen screen) {
        this(x, y, w, h, text, onPress, Ingredient.of(stack), screen);
    }

    public ItemButton withScale(int scale){
        this.scale = scale;
        return this;
    }

    public ItemButton withTooltips(List<Component> tooltips){
        this.tooltips = tooltips;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (ingredient != null && ingredient.getItems().length != 0) {
            ItemStack stack = ingredient.getItems()[(NuggetClientData.ticksInGame / 20) % ingredient.getItems().length];
            if (GuiHelpers.isMouseInRelativeRange(pMouseX, pMouseY, getX(), getY(), width, height)) {
                Font font = Minecraft.getInstance().font;
                List<ClientTooltipComponent> components = new ArrayList<>(GuiHelpers.gatherTooltipComponents(Screen.getTooltipFromItem(Minecraft.getInstance(), stack), pMouseX, screen.width, screen.height, font));
                RenderHelpers.renderTooltipInternal(graphics, components, pMouseX, pMouseY, screen);
            }
            RenderHelpers.drawItemAsIcon(stack, graphics, getX() + 3, getY() + 2, scale, false);
        }
    }

    @Override
    public void getTooltip(List<Component> tooltip) {
        super.getTooltip(tooltip);
        tooltip.addAll(tooltips);
    }
}

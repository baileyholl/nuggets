package com.hollingsworth.nuggets.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.function.Function;

public class NoShadowTextField extends CopyEditBox {

    public Function<String, Void> onClear;

    public NoShadowTextField(Font p_i232260_1_, int p_i232260_2_, int p_i232260_3_, int p_i232260_4_, int p_i232260_5_, Component p_i232260_6_) {
        super(p_i232260_1_, p_i232260_2_, p_i232260_3_, p_i232260_4_, p_i232260_5_, p_i232260_6_);
    }

    public NoShadowTextField(Font p_i232259_1_, int p_i232259_2_, int p_i232259_3_, int p_i232259_4_, int p_i232259_5_, @Nullable EditBox p_i232259_6_, Component p_i232259_7_) {
        super(p_i232259_1_, p_i232259_2_, p_i232259_3_, p_i232259_4_, p_i232259_5_, p_i232259_6_, p_i232259_7_);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack matrixStack = graphics.pose();
        if(!this.visible){
            return;
        }

        int i2 = this.isEditable ? this.textColor : this.textColorUneditable;
        int j = this.cursorPos - this.displayPos;
        int k = this.highlightPos - this.displayPos;
        String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
        boolean flag = j >= 0 && j <= s.length();
        boolean flag1 = this.isFocused() && (Util.getMillis() - this.focusedTime) / 300L % 2L == 0L && flag;
        int l = this.bordered ? this.getX() + 4 : this.getX();
        int i1 = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
        int j1 = l;
        if (k > s.length()) {
            k = s.length();
        }

        if (!s.isEmpty()) {
            String s1 = flag ? s.substring(0, j) : s;
            j1 = graphics.drawString(font, this.formatter.apply(s1, this.displayPos),  l,  i1, -8355712, false);

        }

        boolean flag2 = this.cursorPos < this.value.length() || this.value.length() >= 32;
        int k1 = j1;
        if (!flag) {
            k1 = j > 0 ? l + this.width : l;
        } else if (flag2) {
            k1 = j1 - 1;
            --j1;
        }

        if (!s.isEmpty() && flag && j < s.length()) {
            graphics.drawString(font, this.formatter.apply(s.substring(j), this.cursorPos), j1, i1, i2);
        }

        if (!flag2 && this.suggestion != null) {
            graphics.drawString(this.font, this.suggestion, k1 - 1, i1, -8355712, false);
        }

        if (flag1) {
            if (flag2) {
                graphics.fill(k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
            } else {
                graphics.drawString(this.font, "_", k1, i1, i2, false);
            }
        }

    }

    @Override
    public boolean mouseClicked(double clickedX, double clickedY, int mouseButton) { // 0 for primary, 1 for secondary
        if (!this.isVisible()) {
            return false;
        } else {
            boolean clickedThis = clickedX >= (double) this.getX() && clickedX < (double) (this.getX() + this.width) && clickedY >= (double) this.getY() && clickedY < (double) (this.getY() + this.height);
            if (this.canLoseFocus) {
                this.setFocused(clickedThis);
            }

            if (this.isFocused() && clickedThis && mouseButton == 0) {
                int i = Mth.floor(clickedX) - this.getX();
                if (this.bordered) {
                    i -= 4;
                }

                String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
                this.moveCursorTo(this.font.plainSubstrByWidth(s, i).length() + this.displayPos, true);
                return true;
            } else if (this.isFocused() && mouseButton == 1) {
                if (this.value.isEmpty())
                    return clickedThis;


                if (onClear != null)
                    onClear.apply("");
                setValue("");
                return clickedThis;
            } else {
                return false;
            }
        }
    }
}

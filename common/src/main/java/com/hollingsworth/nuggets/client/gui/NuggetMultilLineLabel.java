package com.hollingsworth.nuggets.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public interface NuggetMultilLineLabel {

    static MultiLineLabel create(Font font, Component... components) {
        return create(font, Integer.MAX_VALUE, Integer.MAX_VALUE, components);
    }

    static MultiLineLabel create(Font font, int maxWidth, Component... components) {
        return create(font, maxWidth, Integer.MAX_VALUE, components);
    }

    static MultiLineLabel create(Font font, Component component, int maxWidth) {
        return create(font, maxWidth, Integer.MAX_VALUE, component);
    }

    static MultiLineLabel create(final Font font, final int maxWidth, final int maxRows, final Component... components) {
        MultiLineLabel vanillaLabel = MultiLineLabel.create(font, maxWidth, maxRows, components);
        return components.length == 0 ? MultiLineLabel.EMPTY : new MultiLineLabel() {
            @Nullable
            private List<TextAndWidth> cachedTextAndWidth;
            @Nullable
            private Language splitWithLanguage;

            @Override
            public void renderCentered(GuiGraphics p_281603_, int p_281267_, int p_281819_) {
                vanillaLabel.renderCentered(p_281603_, p_281267_, p_281819_, 9, -1);
            }

            @Override
            public void renderCentered(GuiGraphics p_283492_, int p_283184_, int p_282078_, int p_352944_, int p_352919_) {
                vanillaLabel.renderCentered(p_283492_, p_283184_, p_282078_, p_352944_, p_352919_);
            }

            public void renderCenteredNoShadow(GuiGraphics p_283492_, int p_283184_, int p_282078_, int p_352944_) {
                this.renderCenteredNoShadow(p_283492_, p_283184_, p_282078_, p_352944_, -1);
            }

            public void renderCenteredNoShadow(GuiGraphics p_283492_, int p_283184_, int p_282078_, int p_352944_, int p_352919_) {
                int i = p_282078_;

                for (MultiLineLabel.TextAndWidth multilinelabel$textandwidth : this.getSplitMessage()) {
                    GuiHelpers.drawCenteredStringNoShadow(font, p_283492_, multilinelabel$textandwidth.text(), p_283184_, i, p_352919_);
                    p_283492_.drawCenteredString(font, multilinelabel$textandwidth.text(), p_283184_, i, p_352919_);
                    i += p_352944_;
                }
            }

            @Override
            public void renderLeftAligned(GuiGraphics p_282318_, int p_283665_, int p_283416_, int p_281919_, int p_281686_) {
                vanillaLabel.renderLeftAligned(p_282318_, p_283665_, p_283416_, p_281919_, p_281686_);
            }

            @Override
            public int renderLeftAlignedNoShadow(GuiGraphics p_281782_, int p_282841_, int p_283554_, int p_282768_, int p_283499_) {
                return vanillaLabel.renderLeftAlignedNoShadow(p_281782_, p_282841_, p_283554_, p_282768_, p_283499_);
            }

            private List<MultiLineLabel.TextAndWidth> getSplitMessage() {
                Language language = Language.getInstance();
                if (this.cachedTextAndWidth != null && language == this.splitWithLanguage) {
                    return this.cachedTextAndWidth;
                } else {
                    this.splitWithLanguage = language;
                    List<FormattedCharSequence> list = new ArrayList<>();

                    for (Component component : components) {
                        list.addAll(font.split(component, maxWidth));
                    }

                    this.cachedTextAndWidth = new ArrayList<>();

                    for (FormattedCharSequence formattedcharsequence : list.subList(0, Math.min(list.size(), maxRows))) {
                        this.cachedTextAndWidth.add(new MultiLineLabel.TextAndWidth(formattedcharsequence, font.width(formattedcharsequence)));
                    }

                    return this.cachedTextAndWidth;
                }
            }

            @Override
            public int getLineCount() {
                return this.getSplitMessage().size();
            }

            @Override
            public int getWidth() {
                return Math.min(maxWidth, this.getSplitMessage().stream().mapToInt(MultiLineLabel.TextAndWidth::width).max().orElse(0));
            }
        };
    }
}

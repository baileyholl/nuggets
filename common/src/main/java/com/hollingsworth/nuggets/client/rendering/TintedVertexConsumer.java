package com.hollingsworth.nuggets.client.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;

public final class TintedVertexConsumer implements VertexConsumer {
    private final VertexConsumer wrapped;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;

    public TintedVertexConsumer(VertexConsumer wrapped, float red, float green, float blue, float alpha) {
        this.wrapped = wrapped;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        wrapped.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        wrapped.setColor(
                (int) (red * this.red),
                (int) (green * this.green),
                (int) (blue * this.blue),
                (int) (alpha * this.alpha)
        );
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        wrapped.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int p_350815_, int p_350629_) {
        wrapped.setUv1(p_350815_, p_350629_);
        return this;
    }

    @Override
    public VertexConsumer setOverlay(int p_350697_) {
        wrapped.setOverlay(p_350697_);
        return this;
    }


    @Override
    public VertexConsumer setUv2(int u, int v) {
        wrapped.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        wrapped.setNormal(x, y, z);
        return this;
    }

}

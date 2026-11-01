package com.hollingsworth.nuggets.client.scene;

public record LytSize(int width, int height) {

    public static LytSize empty() {
        return new LytSize(0, 0);
    }

    public LytSize scaledToFit(LytSize boundary) {
        int bound_width = boundary.width();
        int bound_height = boundary.height();
        int new_width = width;
        int new_height = height;

        // first check if we need to scale width
        if (width > bound_width) {
            //scale width to fit
            new_width = bound_width;
            //scale height to maintain aspect ratio
            new_height = (new_width * height) / width;
        }

        // then check if we need to scale even with the new height
        if (new_height > bound_height) {
            //scale height to fit instead
            new_height = bound_height;
            //scale width to maintain aspect ratio
            new_width = (new_height * width) / height;
        }

        return new LytSize(new_width, new_height);
    }
}

package com.hollingsworth.nuggets.client.scene;

import com.hollingsworth.nuggets.client.rendering.StatePos;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Scene {

    private final Supplier<Stream<BlockPos>> filledBlocks;

    private final CameraSettings cameraSettings;

    private final SavedCameraSettings initialCameraSettings;

    public Scene(Supplier<Stream<BlockPos>> filledBlocks, CameraSettings cameraSettings) {
        this.filledBlocks = filledBlocks;
        this.cameraSettings = cameraSettings;
        this.initialCameraSettings = cameraSettings.save();
    }

    public static Scene of(List<StatePos> blocks, CameraSettings cameraSettings) {
        return new Scene(() -> blocks.stream().filter(statePos -> !statePos.state.isAir()).map(statePos -> statePos.pos), cameraSettings);
    }

    public Vector4f getScreenBounds() {
        var offx = cameraSettings.getOffsetX();
        var offy = cameraSettings.getOffsetY();
        cameraSettings.setOffsetX(0);
        cameraSettings.setOffsetY(0);
        var viewMatrix = cameraSettings.getViewMatrix();
        cameraSettings.setOffsetX(offx);
        cameraSettings.setOffsetY(offy);

        var result = getBounds(viewMatrix);

        return new Vector4f(
                result.min().x,
                result.min().y,
                result.max().x,
                result.max().y);
    }

    public void centerScene() {
        var bounds = getScreenBounds();
        var w = -(bounds.z - bounds.x) / 2;
        var h = -(bounds.w - bounds.y) / 2;
        cameraSettings.setOffsetX(w - bounds.x);
        cameraSettings.setOffsetY(h - bounds.y);
    }

    public LytSize getPreferredSize() {
        // Compute bounds using the *initial* camera settings
        var current = cameraSettings.save();
        cameraSettings.restore(initialCameraSettings);
        var screenBounds = getScreenBounds();
        cameraSettings.restore(current);

        var width = (int) Math.ceil(Math.abs(screenBounds.z - screenBounds.x));
        var height = (int) Math.ceil(Math.abs(screenBounds.w - screenBounds.y));
        return new LytSize(width, height);
    }

    Bounds cachedBounds = null;

    @NotNull
    private Bounds getBounds(Matrix4f viewMatrix) {
        if (cachedBounds != null) {
            return cachedBounds;
        }

        // This is doing more work than needed since touching blocks create unneeded corners
        var tmpPos = new Vector3f();
        var min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        var max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        filledBlocks.get().forEach(pos -> {
            for (var xCorner = 0; xCorner <= 1; xCorner++) {
                for (var yCorner = 0; yCorner <= 1; yCorner++) {
                    for (var zCorner = 0; zCorner <= 1; zCorner++) {
                        viewMatrix.transformPosition(
                                pos.getX() + xCorner,
                                pos.getY() + yCorner,
                                pos.getZ() + zCorner,
                                tmpPos);
                        min.min(tmpPos);
                        max.max(tmpPos);
                    }
                }
            }
        });
        if (min.x == Float.POSITIVE_INFINITY) {
            return new Bounds(new Vector3f(), new Vector3f());
        }
        cachedBounds = new Bounds(min, max);
        return cachedBounds;
    }

    private record Bounds(Vector3f min, Vector3f max) {
    }
    Vector3f cachedCenter = null;
    public Vector3fc getWorldCenter() {
        if(cachedCenter != null){
            return cachedCenter;
        }
        // This is doing more work than needed since touching blocks create unneeded corners
        var tmpPos = new Vector3f();
        var min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        var max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        filledBlocks.get().forEach(pos -> {
            var tmp = new Vector3f();
            for (var xCorner = 0; xCorner <= 1; xCorner++) {
                for (var yCorner = 0; yCorner <= 1; yCorner++) {
                    for (var zCorner = 0; zCorner <= 1; zCorner++) {
                        tmp.set(pos.getX(), pos.getY(), pos.getZ());
                        min.min(tmp);
                        max.max(tmp);

                        tmp.add(1, 1, 1);
                        min.min(tmp);
                        max.max(tmp);
                    }
                }
            }
        });
        var avg = new Vector3f(min);
        avg.add(max);
        avg.div(2);
        cachedCenter = avg;
        return cachedCenter;
    }

    /**
     * The camera settings affect layout so this should be called before layout is done (or relayout should be
     * triggered).
     */
    public CameraSettings getCameraSettings() {
        return cameraSettings;
    }
}

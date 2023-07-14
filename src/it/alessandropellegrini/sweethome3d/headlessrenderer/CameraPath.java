/* SPDX-FileCopyrightText: 2023 Alessandro Pellegrini <alessandro.pellegrini87@gmail.com>
 * SPDX-FileCopyrightText: 2006-2023 Emmanuel Puybaret <http://www.sweethome3d.com/>
 * SPDX-License-Identifier: GPL-3.0-only
 */

package it.alessandropellegrini.sweethome3d.headlessrenderer;

import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Home;

import javax.vecmath.Point3f;
import java.util.ArrayList;
import java.util.List;

/**
 * A path of {@link com.eteks.sweethome3d.model.Camera} that can be used to render a video.
 * The sequence of {@link com.eteks.sweethome3d.model.Camera}s is extracted from the
 * {@link com.eteks.sweethome3d.model.Home} object passed to the constructor.
 * This sequence is then interpolated to obtain a smooth video, abiding by the fps and speed
 * specification passed to the constructor.
 */
public class CameraPath {
    private final List<Camera> videoFramesPath = new ArrayList<>();
    private final float moveDistancePerFrame;
    private final float moveAnglePerFrame;
    private final float elapsedTimePerFrame;
    private final int fps;

    /**
     * Compute the path of the video frames.
     *
     * @param home the home to render
     * @param speed the speed of the camera in m/s
     * @param fps the number of frames per second
     */
    public CameraPath(Home home, float speed, int fps) {
        moveDistancePerFrame = speed * 100f / fps;  // speed is in m/s
        moveAnglePerFrame = (float) (Math.PI / 120 * 30 * speed / fps);
        elapsedTimePerFrame = 345600.0f / fps * 25; // 250 frame/day at 25 frame/second
        this.fps = fps;

        this.computeVideoFramesPath(home);
    }

    public int getFps() {
        return fps;
    }

    public int size() {
        return videoFramesPath.size();
    }

    public Camera get(int index) {
        return videoFramesPath.get(index);
    }

    /**
     * Compute the path of the video frames. This code is taken from
     * {@link com.eteks.sweethome3d.swing.VideoPanel#getVideoFramesPath(float, int)}
     * that is unfortunately private.
     *
     * @param home the {@link com.eteks.sweethome3d.model.Home} to compute the path from
     * @author Emmanuel Puybaret
     */
    private void computeVideoFramesPath(Home home) {
        List<Camera> cameraPath = home.getEnvironment().getVideoCameraPath();
        Camera camera = cameraPath.get(0);
        float x = camera.getX();
        float y = camera.getY();
        float z = camera.getZ();
        float yaw = camera.getYaw();
        float pitch = camera.getPitch();
        float fieldOfView = camera.getFieldOfView();
        long time = camera.getTime();
        videoFramesPath.add(camera.clone());

        for (int i = 1; i < cameraPath.size(); i++) {
            camera = cameraPath.get(i);
            float newX = camera.getX();
            float newY = camera.getY();
            float newZ = camera.getZ();
            float newYaw = camera.getYaw();
            float newPitch = camera.getPitch();
            float newFieldOfView = camera.getFieldOfView();
            long newTime = camera.getTime();

            float distance = new Point3f(x, y, z).distance(new Point3f(newX, newY, newZ));
            float moveCount = distance / moveDistancePerFrame;
            float yawAngleCount = Math.abs(newYaw - yaw) / moveAnglePerFrame;
            float pitchAngleCount = Math.abs(newPitch - pitch) / moveAnglePerFrame;
            float fieldOfViewAngleCount = Math.abs(newFieldOfView - fieldOfView) / moveAnglePerFrame;
            float timeCount = Math.abs(newTime - time) / elapsedTimePerFrame;

            int frameCount = (int) Math.max(moveCount, Math.max(yawAngleCount,
                    Math.max(pitchAngleCount, Math.max(fieldOfViewAngleCount, timeCount))));

            float deltaX = (newX - x) / frameCount;
            float deltaY = (newY - y) / frameCount;
            float deltaZ = (newZ - z) / frameCount;
            float deltaYawAngle = (newYaw - yaw) / frameCount;
            float deltaPitchAngle = (newPitch - pitch) / frameCount;
            float deltaFieldOfViewAngle = (newFieldOfView - fieldOfView) / frameCount;
            long deltaTime = Math.round(((double) newTime - time) / frameCount);

            for (int j = 1; j <= frameCount; j++) {
                Camera pathCamera = camera.clone();
                pathCamera.setX(x + deltaX * j);
                pathCamera.setY(y + deltaY * j);
                pathCamera.setZ(z + deltaZ * j);
                pathCamera.setYaw(yaw + deltaYawAngle * j);
                pathCamera.setPitch(pitch + deltaPitchAngle * j);
                pathCamera.setFieldOfView(fieldOfView + deltaFieldOfViewAngle * j);
                pathCamera.setTime(time + deltaTime * j);
                pathCamera.setLens(Camera.Lens.PINHOLE);
                videoFramesPath.add(pathCamera);
            }

            x = newX;
            y = newY;
            z = newZ;
            yaw = newYaw;
            pitch = newPitch;
            fieldOfView = newFieldOfView;
            time = newTime;
        }
    }
}

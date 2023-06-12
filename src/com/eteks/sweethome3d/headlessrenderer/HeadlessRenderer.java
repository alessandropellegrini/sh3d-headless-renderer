/*
 * ConsolePhotoGenerator.java
 *
 * Sweet Home 3D, Copyright (c) 2023 Alessandro Pellegrini <alessandro.pellegrini87@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.headlessrenderer;

import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.j3d.AbstractPhotoRenderer;
import com.eteks.sweethome3d.j3d.Object3DBranchFactory;
import com.eteks.sweethome3d.j3d.YafarayRenderer;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.swing.JPEGImagesToVideo;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.vecmath.Point3f;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Sweet Home 3D headless video generator.
 *
 * @author Alessandro Pellegrini
 */
@Command(name = "headlessrenderer", mixinStandardHelpOptions = true, version = "headlessrenderer 1.0",
        description = "Headless Video Renderer of SH3D files")
public class HeadlessRenderer implements Callable<Integer> {

    @Option(names = { "-w", "--width" }, description = "Redering width")
    Integer width = null;

    @Option(names = { "-h", "--height" }, description = "Redering height")
    Integer height = null;

    @Option(names = { "-f", "--fps" }, description = "Frames per second")
    Integer fps = null;

    @Option(names = { "-s", "--speed" }, description = "Camera speed")
    Integer speed = null;

    @Option(names = { "-i", "--input" }, description = "SH3D File")
    File input = null;

    @Option(names = { "-o", "--output" }, description = "output file to create")
    File output;

    final AbstractPhotoRenderer.Quality quality = AbstractPhotoRenderer.Quality.HIGH;
    final JPEGImagesToVideo video = new JPEGImagesToVideo();

    @Override
    public Integer call() throws Exception {
        int ret = 64; // EX_USAGE

        if(this.width != null && this.height != null && this.fps != null && this.speed != null && this.input != null && this.output != null)
            ret = render();
        else
            System.out.println("Missing arguments: use -h to see the help");

        return ret;
    }

    private int render() {
        try {
            Home home = (new HomeFileRecorder()).readHome(String.valueOf(input));
            YafarayRenderer yafaRenderer = new YafarayRenderer(home, new Object3DBranchFactory(), quality);
            final Camera[] videoFramesPath = getVideoFramesPath(home, speed, fps);
            PhotoImageGenerator frameGenerator = new PhotoImageGenerator(width, height, yafaRenderer);
            ImageDataSource sourceStream = new ImageDataSource(new VideoFormat(VideoFormat.JPEG, new Dimension(800, 600),
                    Format.NOT_SPECIFIED, Format.byteArray, 8), frameGenerator, videoFramesPath);

            System.out.println("Creating video file: " + output + " (" + videoFramesPath.length + " frames)");
            video.createVideoFile(800, 600, 25, sourceStream, output);
        } catch (RecorderException | IOException e) {
            return 1;
        }

        return 0;
    }

    public static void main(final String[] args) throws RecorderException, IOException, NoSuchFieldException, IllegalAccessException {
        int exitCode = new CommandLine(new HeadlessRenderer()).execute(args);
        System.exit(exitCode);
    }


    private static Camera[] getVideoFramesPath(Home home, final float speed, int frameRate) {
        java.util.List<Camera> videoFramesPath = new ArrayList<>();
        final float moveDistancePerFrame = speed * 100f / frameRate;  // speed is in m/s
        final float moveAnglePerFrame = (float) (Math.PI / 120 * 30 * speed / frameRate);
        final float elapsedTimePerFrame = 345600.0f / frameRate * 25; // 250 frame/day at 25 frame/second

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

        return videoFramesPath.toArray(new Camera[0]);
    }


    private static class ImageDataSource extends PullBufferDataSource {
        private final ImageSourceStream stream;

        public ImageDataSource(VideoFormat format, PhotoImageGenerator frameGenerator, Camera[] framesPath) {
            this.stream = new ImageSourceStream(format, frameGenerator, framesPath);
        }

        @Override
        public MediaLocator getLocator() {
            return null;
        }

        /**
         * Returns RAW since buffers of video frames are sent without a container format.
         */
        @Override
        public String getContentType() {
            return ContentDescriptor.RAW;
        }

        @Override
        public void connect() {
        }

        @Override
        public void disconnect() {
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        public PullBufferStream[] getStreams() {
            return new PullBufferStream[]{this.stream};
        }

        /**
         * Not necessary to compute the duration.
         */
        @Override
        public Time getDuration() {
            return DURATION_UNKNOWN;
        }

        @Override
        public Object[] getControls() {
            return new Object[0];
        }

        @Override
        public Object getControl(String type) {
            return null;
        }
    }

    /**
     * A source of video images.
     */
    private static class ImageSourceStream implements PullBufferStream {
        private final PhotoImageGenerator frameGenerator;
        private final Camera[] framesPath;
        private final VideoFormat format;
        private int imageIndex;
        private final Instant start = Instant.now();

        public ImageSourceStream(VideoFormat format, PhotoImageGenerator frameGenerator, Camera[] framesPath) {
            this.frameGenerator = frameGenerator;
            this.framesPath = framesPath;
            this.format = format;
        }

        @Override
        public boolean willReadBlock() {
            return false;
        }

        /**
         * This is called from the Processor to read a frame worth of video data.
         */
        @Override
        public void read(Buffer buffer) throws IOException {
            buffer.setOffset(0);
            // Check if we've finished all the frames
            if (endOfStream()) {
                buffer.setEOM(true);
                buffer.setLength(0);
            } else {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BufferedImage frame = this.frameGenerator.renderImageAt(this.framesPath[this.imageIndex],
                        this.imageIndex == this.framesPath.length - 1);
                ImageIO.write(frame, "JPEG", outputStream);
                byte[] data = outputStream.toByteArray();
                buffer.setData(data);
                buffer.setLength(data.length);
                buffer.setFormat(this.format);
                buffer.setFlags(buffer.getFlags() | Buffer.FLAG_KEY_FRAME);

                final int progressionValue = this.imageIndex++;
                Instant now = Instant.now();
                Duration elapsed = Duration.between(this.start, now);
                double perFrame = (double) elapsed.getSeconds() / this.imageIndex;
                Duration remaining = Duration.ofSeconds((long) (perFrame * (this.framesPath.length - this.imageIndex)));

                System.out.println("Processed frame " + (progressionValue+1) + "/" + this.framesPath.length + " in "
                        + formatDuration(elapsed) + ". ETA: " + formatDuration(remaining));
            }
        }

        private String formatDuration(Duration duration) {
            return String.format("%d:%02d:%02d",
                    duration.toHours(),
                    duration.toMinutesPart(),
                    duration.toSecondsPart());
        }

        @Override
        public Format getFormat() {
            return format;
        }

        @Override
        public ContentDescriptor getContentDescriptor() {
            return new ContentDescriptor(ContentDescriptor.RAW);
        }

        @Override
        public long getContentLength() {
            return 0;
        }

        @Override
        public boolean endOfStream() {
            return this.imageIndex == this.framesPath.length;
        }

        @Override
        public Object[] getControls() {
            return new Object[0];
        }

        @Override
        public Object getControl(String type) {
            return null;
        }
    }

    private static class PhotoImageGenerator {
        private AbstractPhotoRenderer renderer;
        private final BufferedImage image;

        public PhotoImageGenerator(int width, int height, AbstractPhotoRenderer renderer) {
            this.renderer = renderer;
            this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        public BufferedImage renderImageAt(Camera frameCamera, boolean last) throws IOException {
            try {
                this.renderer.render(this.image, frameCamera, null);
                return image;
            } catch (InterruptedIOException ex) {
                this.renderer = null;
                throw ex;
            } finally {
                if (last) {
                    assert this.renderer != null;
                    this.renderer.dispose();
                    this.renderer = null;
                }
            }
        }
    }
}
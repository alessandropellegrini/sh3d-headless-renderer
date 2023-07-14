/*
 * SPDX-FileCopyrightText: 2023 Alessandro Pellegrini <alessandro.pellegrini87@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.alessandropellegrini.sweethome3d.headlessrenderer;

import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class ImageDataSource extends PullBufferDataSource {
    private final ImageSourceStream stream;

    public ImageDataSource(CameraRenderer renderer, CameraPath path) {
        this.stream = new ImageSourceStream(renderer, path);
    }

    @Override
    public MediaLocator getLocator() {
        return null;
    }

    @Override
    public String getContentType() {
        return ContentDescriptor.RAW; // No container format
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

    @Override
    public Time getDuration() {
        return DURATION_UNKNOWN; // Unnecessary
    }

    @Override
    public Object[] getControls() {
        return new Object[0]; // Unnecessary
    }

    @Override
    public Object getControl(String type) {
        return null; // Unnecessary
    }

    public int size() {
        return this.stream.size();
    }

    private static class ImageSourceStream implements PullBufferStream {
        private final CameraRenderer renderer;
        private final CameraPath path;
        private final VideoFormat format;
        private int imageIndex = 0;
        private Instant start = null;

        public ImageSourceStream(CameraRenderer renderer, CameraPath path) {
            this.renderer = renderer;
            this.format = new VideoFormat(VideoFormat.JPEG, new Dimension(renderer.width(), renderer.height()),
                    Format.NOT_SPECIFIED, Format.byteArray, path.getFps());

            this.path = path;
        }

        @Override
        public boolean willReadBlock() {
            return false;
        }

        @Override
        public void read(Buffer buffer) throws IOException {
            if (this.start == null) {
                this.start = Instant.now();
            }

            buffer.setOffset(0);

            if (endOfStream()) {
                buffer.setEOM(true);
                buffer.setLength(0);
            } else {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BufferedImage frame = this.renderer.renderFrameAt(this.path.get(this.imageIndex));
                ImageIO.write(frame, "JPEG", outputStream);
                byte[] data = outputStream.toByteArray();
                buffer.setData(data);
                buffer.setLength(data.length);
                buffer.setFormat(this.format);
                buffer.setFlags(buffer.getFlags() | Buffer.FLAG_KEY_FRAME);

                ++this.imageIndex;
                this.progress();
            }
        }

        private void progress() {
            Instant now = Instant.now();
            Duration elapsed = Duration.between(this.start, now);
            double perFrame = (double) elapsed.getSeconds() / this.imageIndex;
            Duration remaining = Duration.ofSeconds((long) (perFrame * (this.path.size() - this.imageIndex)));

            System.out.println("Processed frame " + this.imageIndex + "/" + this.path.size() + " in "
                    + formatDuration(elapsed) + ". ETA: " + formatDuration(remaining));
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
            return this.imageIndex == this.path.size();
        }

        @Override
        public Object[] getControls() {
            return new Object[0];
        }

        @Override
        public Object getControl(String type) {
            return null;
        }

        public int size() {
            return this.path.size();
        }
    }
}
/*
 * SPDX-FileCopyrightText: 2023 Alessandro Pellegrini <alessandro.pellegrini87@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.alessandropellegrini.sweethome3d.headlessrenderer;

import com.eteks.sweethome3d.io.HomeFileRecorder;
import com.eteks.sweethome3d.j3d.AbstractPhotoRenderer;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.RecorderException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import javax.media.IncompatibleSourceException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
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

    @Option(names = { "-s", "--speed" }, description = "Camera speed (m/s)")
    Float speed = null;

    @Option(names = { "-i", "--input" }, description = "SH3D File")
    File input = null;

    @Option(names = { "-o", "--output" }, description = "output file to create")
    File output;

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
            CameraPath path = new CameraPath(home, speed, fps);
            CameraRenderer renderer = new CameraRenderer(home, width, height);
            QuickTimeEncoder encoder = new QuickTimeEncoder(output, renderer, path);
            encoder.start();
        } catch (RecorderException | IOException | IncompatibleSourceException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        return 0;
    }

    public static void main(final String[] args) throws RecorderException, IOException, NoSuchFieldException, IllegalAccessException {
        int exitCode = new CommandLine(new HeadlessRenderer()).execute(args);
        System.exit(exitCode);
    }

}
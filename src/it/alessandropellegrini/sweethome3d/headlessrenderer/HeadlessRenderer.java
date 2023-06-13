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
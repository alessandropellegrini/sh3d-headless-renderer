package it.alessandropellegrini.sweethome3d.headlessrenderer;

import com.eteks.sweethome3d.j3d.AbstractPhotoRenderer;
import com.eteks.sweethome3d.j3d.Object3DBranchFactory;
import com.eteks.sweethome3d.j3d.YafarayRenderer;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Home;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class CameraRenderer {
    private final YafarayRenderer renderer;
    private final int width;
    private final int height;

    private CameraRenderer() {
        this.renderer = null;
        this.width = 0;
        this.height = 0;
    }

    public CameraRenderer(Home home, final int width, final int height) throws IOException {
        this.renderer = new YafarayRenderer(home, new Object3DBranchFactory(), AbstractPhotoRenderer.Quality.HIGH);
        this.height = height;
        this.width = width;
    }

    public BufferedImage renderFrameAt(Camera frameCamera) throws IOException {
        final BufferedImage frame = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
        this.renderer.render(frame, frameCamera, null);
        return frame;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }
}

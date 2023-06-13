package it.alessandropellegrini.sweethome3d.headlessrenderer.jmt;

import com.sun.media.BasicProcessor;

import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import java.awt.*;
import java.io.IOException;

public class HeadlessMediaProcessor extends BasicProcessor {

    protected HeadlessPlaybackEngine engine = new HeadlessPlaybackEngine(this);

    public HeadlessMediaProcessor() {
    }

    public void setSource(DataSource source) throws IOException, IncompatibleSourceException {
        this.engine.setSource(source);
        this.manageController(this.engine);
        super.setSource(source);
    }

    public Component getVisualComponent() {
        super.getVisualComponent();
        return this.engine.getVisualComponent();
    }

    public GainControl getGainControl() {
        super.getGainControl();
        return this.engine.getGainControl();
    }

    public Time getMediaTime() {
        return super.controllerList.size() > 1 ? super.getMediaTime() : this.engine.getMediaTime();
    }

    public long getMediaNanoseconds() {
        return super.controllerList.size() > 1 ? super.getMediaNanoseconds() : this.engine.getMediaNanoseconds();
    }

    protected TimeBase getMasterTimeBase() {
        return this.engine.getTimeBase();
    }

    protected boolean audioEnabled() {
        return this.engine.audioEnabled();
    }

    protected boolean videoEnabled() {
        return this.engine.videoEnabled();
    }

    public TrackControl[] getTrackControls() throws NotConfiguredError {
        return this.engine.getTrackControls();
    }

    public ContentDescriptor[] getSupportedContentDescriptors() throws NotConfiguredError {
        return this.engine.getSupportedContentDescriptors();
    }

    public ContentDescriptor setContentDescriptor(ContentDescriptor ocd) throws NotConfiguredError {
        return this.engine.setContentDescriptor(ocd);
    }

    public ContentDescriptor getContentDescriptor() throws NotConfiguredError {
        return this.engine.getContentDescriptor();
    }

    public DataSource getDataOutput() throws NotRealizedError {
        return this.engine.getDataOutput();
    }

    public void updateStats() {
        this.engine.updateRates();
    }
}
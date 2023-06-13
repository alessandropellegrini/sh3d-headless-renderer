package it.alessandropellegrini.sweethome3d.headlessrenderer;

import it.alessandropellegrini.sweethome3d.headlessrenderer.jmt.HeadlessMediaProcessor;

import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;

public class QuickTimeEncoder {

    private final Object waitSync = new Object();
    private boolean stateTransitionOk = true;
    private final Object waitFileSync = new Object();
    private boolean fileDone = false;
    private String fileError = null;

    private final File output;
    private final ImageDataSource dataSource;

    private QuickTimeEncoder() {
        this.output = null;
        this.dataSource = null;
    }

    public QuickTimeEncoder(File output, CameraRenderer renderer, CameraPath path) {
        this.output = output;
        this.dataSource = new ImageDataSource(renderer, path);
    }

    /**
     * Start the JMT pipeline to encode the video.
     * This code is partially based on original code by Sun Microsystems.
     *
     * @throws IOException if an error occurs while encoding the video
     */
    public void start() throws IOException, IncompatibleSourceException {
        assert (this.dataSource != null);
        assert (this.output != null);

        System.out.println("Creating video file: " + output + " (" + this.dataSource.size() + " frames)");

        ControllerListener controllerListener = ev -> {
            synchronized (waitSync) {
                if (ev instanceof ConfigureCompleteEvent || ev instanceof RealizeCompleteEvent || ev instanceof PrefetchCompleteEvent) {
                    stateTransitionOk = true;
                } else if (ev instanceof ResourceUnavailableEvent) {
                    stateTransitionOk = false;
                } else if (ev instanceof EndOfMediaEvent) {
                    ev.getSourceController().stop();
                    ev.getSourceController().close();
                }
                waitSync.notifyAll();
            }
        };

        DataSinkListener dataSinkListener = ev -> {
            synchronized (waitFileSync) {
                if (ev instanceof EndOfStreamEvent) {
                    fileDone = true;
                } else if (ev instanceof DataSinkErrorEvent) {
                    fileDone = true;
                    fileError = "Data sink error";
                }
                waitFileSync.notifyAll();
            }
        };

        Processor processor = null;
        DataSink dataSink = null;
        try {
            processor = this.createProcessor(dataSource);
            processor.addControllerListener(controllerListener);
            processor.configure();
            if (waitForTransitionOk(processor, Processor.Configured)) {
                throw new IOException("Failed to configure the processor.");
            }

            processor.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));

            TrackControl[] trackControls = processor.getTrackControls();
            Format[] format = trackControls[0].getSupportedFormats();
            if (format == null || format.length == 0) {
                throw new IOException("The mux does not support the input format: " + trackControls[0].getFormat());
            }

            trackControls[0].setFormat(format[0]);

            processor.realize();
            if (waitForTransitionOk(processor, Controller.Realized)) {
                throw new IOException("Failed to realize the processor.");
            }

            dataSink = Manager.createDataSink(processor.getDataOutput(), new MediaLocator(this.output.toURI().toURL()));
            dataSink.open();
            dataSink.addDataSinkListener(dataSinkListener);
            this.fileDone = false;

            processor.start();
            dataSink.start();

            synchronized (this.waitFileSync) {
                while (!this.fileDone) {
                    this.waitFileSync.wait();
                }
            }

            if (this.fileError != null) {
                throw new IOException(this.fileError);
            }
        } catch (NoProcessorException ex) {
            throw new IOException(ex.getMessage(), ex);
        } catch (NoDataSinkException ex) {
            throw new IOException("Failed to create a DataSink for the given output MediaLocator", ex);
        } catch (InterruptedException ex) {
            if (dataSink != null) {
                dataSink.stop();
            }
            throw new InterruptedIOException("Video creation interrupted");
        } finally {
            if (dataSink != null) {
                dataSink.close();
                dataSink.removeDataSinkListener(dataSinkListener);
            }
            if (processor != null) {
                processor.close();
                processor.removeControllerListener(controllerListener);
            }
        }
    }

    /**
     * Blocks until the processor has transitioned to the given state. Return false
     * if the transition failed.
     */
    private boolean waitForTransitionOk(Processor p, int state) throws InterruptedException {
        synchronized (waitSync) {
            while (p.getState() < state && stateTransitionOk) {
                waitSync.wait();
            }
        }
        return !stateTransitionOk;
    }

    private Processor createProcessor(DataSource source) throws NoProcessorException, IOException, IncompatibleSourceException {
        Processor handler = new HeadlessMediaProcessor();
        handler.setSource(source);
        return handler;
    }
}

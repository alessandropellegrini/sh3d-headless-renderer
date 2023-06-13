package it.alessandropellegrini.sweethome3d.headlessrenderer.jmt;

import com.sun.media.BasicSourceModule;

import javax.media.Demultiplexer;
import javax.media.IncompatibleSourceException;
import javax.media.protocol.DataSource;
import java.io.IOException;

public class HeadlessSourceModule extends BasicSourceModule {

    protected HeadlessSourceModule(DataSource ds, Demultiplexer demux) {
        super(ds, demux);
    }

    public static HeadlessSourceModule createModule(DataSource ds) throws IOException, IncompatibleSourceException {
        BasicSourceModule module = BasicSourceModule.createModule(ds);
        HeadlessSourceModule headlessSourceModule = null;

        if (module != null) {
            Demultiplexer parser = module.getDemultiplexer();

            if (parser != null) {
                headlessSourceModule = new HeadlessSourceModule(ds, parser);
                headlessSourceModule.jmd = new HeadlessJMD();
            }
        }

        return headlessSourceModule;
    }
}

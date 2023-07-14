/*
 * SPDX-FileCopyrightText: 2023 Alessandro Pellegrini <alessandro.pellegrini87@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.alessandropellegrini.sweethome3d.headlessrenderer.jmt;

import com.sun.media.BasicProcessor;
import com.sun.media.ProcessEngine;

import javax.media.IncompatibleSourceException;
import javax.media.protocol.DataSource;
import java.io.IOException;

public class HeadlessPlaybackEngine extends ProcessEngine {

    public HeadlessPlaybackEngine(BasicProcessor p) {
        super(p);
    }

    public void setSource(DataSource ds) throws IOException, IncompatibleSourceException {
        this.source = HeadlessSourceModule.createModule(ds);

        if (this.source == null) {
            throw new IncompatibleSourceException();
        }

        this.source.setController(this);
        this.dsource = ds;
        this.jmd = new HeadlessJMD();
    }
}

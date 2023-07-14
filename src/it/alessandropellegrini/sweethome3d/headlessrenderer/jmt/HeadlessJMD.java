/*
 * SPDX-FileCopyrightText: 2023 Alessandro Pellegrini <alessandro.pellegrini87@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.alessandropellegrini.sweethome3d.headlessrenderer.jmt;

import com.sun.media.BasicModule;
import com.sun.media.JMD;

import javax.media.Buffer;
import java.awt.*;

public class HeadlessJMD implements JMD {
    @Override
    public void setVisible(boolean b) {
        // no-op
    }

    @Override
    public void initGraph(BasicModule basicModule) {
        // no-op
    }

    @Override
    public void moduleIn(BasicModule basicModule, int i, Buffer buffer, boolean b) {
        // no-op
    }

    @Override
    public void moduleOut(BasicModule basicModule, int i, Buffer buffer, boolean b) {
        // no-op
    }

    @Override
    public Component getControlComponent() {
        return null;
    }
}

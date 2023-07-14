#!/bin/bash
# SPDX-FileCopyrightText: 2023 Alessandro Pellegrini <alessandro.pellegrini87@gmail.com>
# SPDX-License-Identifier: CC0-1.0

java -Dj3d.rend=noop -Djava.awt.headless=true -Djava.library.path=yafaray \
     -classpath bin:libs/jmf.jar:libs/j3dcore.jar:libs/vecmath.jar:libs/j3dutils.jar:libs/gluegen-rt.jar:libs/SweetHome3D.jar:libs/jogl-java3d.jar:libs/sunflow-0.07.3i.jar \
     it.alessandropellegrini.sweethome3d.headlessrenderer.HeadlessRenderer "$@"
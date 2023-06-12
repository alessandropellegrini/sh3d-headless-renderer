#!/bin/bash

java -Djava.library.path=yafaray -classpath bin:libs/jmf.jar:libs/j3dcore.jar:libs/vecmath.jar:libs/j3dutils.jar:libs/gluegen-rt.jar:libs/SweetHome3D.jar:libs/jogl-java3d.jar:libs/sunflow-0.07.3i.jar com.eteks.sweethome3d.headlessrenderer.HeadlessRenderer "$@"
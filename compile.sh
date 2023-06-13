#!/bin/bash

javac -cp .:./libs/* -d bin \
   ./src/picocli/CommandLine.java \
   ./src/it/alessandropellegrini/sweethome3d/headlessrenderer/ImageDataSource.java \
   ./src/it/alessandropellegrini/sweethome3d/headlessrenderer/CameraRenderer.java \
   ./src/it/alessandropellegrini/sweethome3d/headlessrenderer/HeadlessRenderer.java \
   ./src/it/alessandropellegrini/sweethome3d/headlessrenderer/jmt/HeadlessJMD.java \
   ./src/it/alessandropellegrini/sweethome3d/headlessrenderer/jmt/HeadlessPlaybackEngine.java \
   ./src/it/alessandropellegrini/sweethome3d/headlessrenderer/jmt/HeadlessMediaProcessor.java \
   ./src/it/alessandropellegrini/sweethome3d/headlessrenderer/jmt/HeadlessSourceModule.java \
   ./src/it/alessandropellegrini/sweethome3d/headlessrenderer/QuickTimeEncoder.java \
   ./src/it/alessandropellegrini/sweethome3d/headlessrenderer/CameraPath.java

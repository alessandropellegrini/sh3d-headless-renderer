#!/bin/bash
# SPDX-FileCopyrightText: 2023 Alessandro Pellegrini <alessandro.pellegrini87@gmail.com>
# SPDX-License-Identifier: CC0-1.0

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

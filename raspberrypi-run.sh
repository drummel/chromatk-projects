#!/bin/bash

# Create a log filename
LOGFILE=chromatik-run-$(date +"%Y-%m-%d_%H-%M-%S").log
java -cp Chromatik-1.0.1-SNAPSHOT/chromatik-1.0.1-SNAPSHOT-linux-aarch64.jar heronarts.lx.studio.Chromatik --accept-eula --require-license --headless Projects/naga-cool-sparkle.lxp  2>&1 | tee $LOGFILE

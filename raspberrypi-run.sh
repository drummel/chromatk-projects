#!/bin/bash

# Create a log filename with a timestamp
# LOGFILE=$(pwd)/chromatik-run-$(date +"%Y-%m-%d_%H-%M-%S").log

# Use to run manualy and log
# java -cp Chromatik-1.0.1-SNAPSHOT/chromatik-1.0.1-SNAPSHOT-linux-aarch64.jar heronarts.lx.studio.Chromatik --accept-eula --require-license --headless Projects/naga-cool-sparkle.lxp \
# -Dsun.net.dns.spi.nameservice.provider.1=dns,sun \
# -Dsun.net.dns.spi.nameservice.provider.2=mdns,sun \
#  2>&1 | tee $LOGFILE

/home/naga/.sdkman/candidates/java/current/bin/java \
 -Dsun.net.inetaddr.ttl=0 \
 -Dsun.net.dns.spi.nameservice.provider.1=dns,sun \
 -Dsun.net.dns.spi.nameservice.provider.2=mdns,sun \
 -cp Chromatik-1.0.1-SNAPSHOT/chromatik-1.0.1-SNAPSHOT-linux-aarch64.jar heronarts.lx.studio.Chromatik \
 --accept-eula --require-license --headless Projects/naga-ggp.lxp
#!/bin/bash

read -p "Please enter your licence code: " liccode
echo "Your authcode is: $liccode"

wget https://github.com/heronarts/Chromatik/releases/download/1.0.1-SNAPSHOT-2024-11-11/Chromatik-1.0.1-SNAPSHOT-linux-aarch64.zip
unzip Chromatik-1.0.1-SNAPSHOT-linux-aarch64.zip

java -cp Chromatik-1.0.1-SNAPSHOT/chromatik-1.0.1-SNAPSHOT-linux-aarch64.jar heronarts.lx.studio.Chromatik --authorize $liccode --accept-eula --require-license --headless

# Just in case
chmod +x ~/Chromatik/raspberrypi-run.sh
chmod +x ~/Chromatik/raspberrypi-setup-service.sh

# Setup Tailscale
# check if tailscale is installed
if ! command -v tailscale &> /dev/null; then
    curl -fsSL https://pkgs.tailscale.com/install.sh | sh
    tailscale up
fi

# Set up Tailscale subnet route advertising
# Routes are: Shore Wifi, Head Wifi, LTE Mode, (tbd add subnet for angios)
sudo tailscale set --advertise-routes=192.168.7.0/24,192.168.8.0/24,192.168.5.1/32
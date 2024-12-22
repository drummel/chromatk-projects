#!/bin/bash

read -p "Please enter your licence code: " liccode
echo "Your authcode is: $liccode"

wget https://github.com/heronarts/Chromatik/releases/download/1.0.1-SNAPSHOT-2024-11-11/Chromatik-1.0.1-SNAPSHOT-linux-aarch64.zip
unzip Chromatik-1.0.1-SNAPSHOT-linux-aarch64.zip

java -cp chromatik-1.0.1-SNAPSHOT-linux-aarch64.jar heronarts.lx.studio.Chromatik --authorize $liccode --accept-eula --require-license --headless
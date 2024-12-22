# Copy this file to your Raspberry Pi and run it to setup the Chromatik service
sudo cp chromatik.service /etc/systemd/system/chromatik.service
sudo systemctl daemon-reload
sudo systemctl enable chromatik.service
sudo systemctl start chromatik.service

# Check the status
sudo systemctl status chromatik.service


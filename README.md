Copy all the files from your laptop to the pi:

`scp -r ~/Chromatik/Models ~/Chromatik/Fixtures ~/Chromatik/Projects ~/Chromatik/Views naga@naga-pi.local:/home/naga/`

Read the service logs:
`journalctl -u chromatik.service -f`

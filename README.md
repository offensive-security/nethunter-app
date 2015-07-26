nethunter-app modified version with custom kali buttons.

For example I really like to have nmap -Ss IP/24 as a custom button, so I decided to update nethunter!

You need to add the following lines at the end of bootkali:

if [ "$1" == "custom_cmd" ]; then

LANG=C PATH=$PATH:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin $busybox chroot $mnt $2

fi

nethunter-app modified version with custom kali buttons.

For example I really like to have nmap -Ss IP/24 as a custom button, so I decided to update nethunter!

You need to add the following lines at the end of bootkali (before last fi):

if [ "$1" == "custom_cmd" ]; then

  LANG=C PATH=$PATH:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin 
  
  commmand=""
  
  while test $# -gt 0
  
  do
  
    shift
    
    commmand=${commmand}" "$1
    
   done
   
   $busybox chroot $mnt $commmand
   
fi

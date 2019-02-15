#!/bin/bash

# ================== #
# Define global variable
# ================== #
# Set nethunter path
nh_path="/data/local/nhsystem/kali-armhf"
# Shorten chroot command as we execute this script in android environment and sometimes we need to execute the program in Kali environmnet
chroot_nh="chroot $nh_path"
# Set your desired "network portion" address assigned to target here (without full stop at the end)
net_addr="10.0.0"
iface_addr="$net_addr.1"
# Timeout to stop this script when target not getting any ip address
countime=120

# ================== #
# Define shutdown function
# ================== #
shutitdown(){
echo " [!] Bringing down $INTERFACE and revert the iptables"
pkill dhcpd
echo 0 > /proc/sys/net/ipv4/ip_forward
# Revert iptables setting dumped from kernel log.
iptables -t nat -D tetherctrl_nat_POSTROUTING -o  $UPSTREAM -j MASQUERADE
ip6tables -D tetherctrl_FORWARD -g tetherctrl_counters
iptables -D tetherctrl_FORWARD -i  $UPSTREAM -o  $INTERFACE -m state --state ESTABLISHED,RELATED -g tetherctrl_counters
iptables -D tetherctrl_FORWARD -i  $INTERFACE -o  $UPSTREAM -m state --state INVALID -j DROP
iptables -D tetherctrl_FORWARD -i  $INTERFACE -o  $UPSTREAM -g tetherctrl_counters
ip6tables -t raw -D tetherctrl_raw_PREROUTING -i  $INTERFACE -m rpfilter --invert ! -s fe80::/64 -j DROP
iptables -A tetherctrl_FORWARD -j DROP
iptables -D tetherctrl_FORWARD -j DROP
# Bring down interface
ip link set $INTERFACE down
ip addr flush dev $INTERFACE
exit
}

# ================== #
# Check required tools in Android environment
# ================== #
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
# Get upstream interface and check if it is up
# ================== #
if [ "$(route | sed '1,2d' | awk '{print $8}')" != "" ]; then
    UPSTREAM=$(route | sed '1,2d' | awk '{print $8}')
    echo " [+] Interface [$INTERFACE] detected"
    echo " [+] Using [$UPSTREAM] as upstream interface"
else
    echo " [-] Upstream interface not found, please check again!"
    exit
fi

# ================== #
# Bring up interface and setup iptables
# ================== #
ip link set $INTERFACE down
ip addr flush dev $INTERFACE
ip addr add $iface_addr/24 dev $INTERFACE
ip link set $INTERFACE up
# Catch the interrupt signal and run the shutdown script
trap shutitdown INT
# Very important command to allow internet access for target.
ip route append $net_addr.0/24 dev $INTERFACE src $iface_addr proto kernel scope link table $UPSTREAM
#ip route add default via $iface_addr dev $INTERFACE
echo 1 > /proc/sys/net/ipv4/ip_forward
# Setup iptables rules dumped from kernel log
iptables -t nat -A tetherctrl_nat_POSTROUTING -o  $UPSTREAM -j MASQUERADE
ip6tables -A tetherctrl_FORWARD -g tetherctrl_counters
iptables -A tetherctrl_FORWARD -i  $UPSTREAM -o  $INTERFACE -m state --state ESTABLISHED,RELATED -g tetherctrl_counters
iptables -A tetherctrl_FORWARD -i  $INTERFACE -o  $UPSTREAM -m state --state INVALID -j DROP
iptables -A tetherctrl_FORWARD -i  $INTERFACE -o  $UPSTREAM -g tetherctrl_counters
ip6tables -t raw -A tetherctrl_raw_PREROUTING -i  $INTERFACE -m rpfilter --invert ! -s fe80::/64 -j DROP
iptables -D tetherctrl_FORWARD -j DROP
iptables -A tetherctrl_FORWARD -j DROP

# ================== #
# Remove all previous leases, pid and conf files
# ================== #
if [ -s $nh_path/var/lib/dhcp/dhcpd.leases ]; then
    rm -f $nh_path/var/lib/dhcp/dhcpd.leases
fi
if [ -s $nh_path/root/auto_share_network.conf ]; then
    rm $nh_path/root/auto_share_network.conf
fi
if [ -s $nh_path/run/dhcpd.pid ]; then
    rm $nh_path/run/dhcpd.pid
fi

# Recreate leases file
touch $nh_path/var/lib/dhcp/dhcpd.leases

# ================== #
# Create custom dhcpd.conf
# ================== #
cat << EOF > $nh_path/root/auto_share_network.conf
ddns-update-style none;
default-lease-time 6000;
max-lease-time 7200;
authoritative;
log-facility local7;
subnet $net_addr.0 netmask 255.255.255.0 {
  range $net_addr.100 $net_addr.150;
  option routers $iface_addr;
  option domain-name "local";
  #option domain-name-servers $iface_addr;
  option domain-name-servers $(getprop net.dns1);
  option domain-name-servers 8.8.8.8;
}
EOF

# ================== #
# Start dhcpd
# ================== #
$chroot_nh /usr/sbin/dhcpd -cf /root/auto_share_network.conf -q

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
# Enter to kill program
# ================== #
echo " [+] BRING IT ON !! LETS ROCK & ROLL !!"
echo " [!] And DONT Forget to Hit Enter to Kill Me"
read

# Last step, shutdown
shutitdown
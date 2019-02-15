#!/system/bin/sh

while true; do
echo " Which environment do you want?\n"
echo "    1. kali || 2. android su || 3. android || 99. exit\n"
echo -n "  Option: "
read option

if [ "$option" = "99" ]; then
    echo
    echo " [!] Have a nice day!!"
    read
    exit
elif [ "$option" = 1 ]; then
    su -c bootkali_login
    break
elif [ "$option" = 2 ]; then
    su
    break
elif [ "$option" = 3 ]; then
    break
else
    echo " [-] Invalid option!! Please input again."
    read
    clear
    continue
fi
done
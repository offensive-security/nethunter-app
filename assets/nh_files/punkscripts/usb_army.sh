# This is a scripted to execute several types of USB Attack on your nethunter device.
# please note that hid function is NOT always set to enabled unless you switch on the hid function.

#!/system/bin/sh
# ============== #
# Define globle variables 
# ============== #
startusb="setprop sys.usb.config "
suffix=""
config[0]="reset"
config+=("hid")
config+=("mass_storage")
config+=("")
config+=("hid,mass_storage")
config+=("")
config+=("")
config+=("")

# Define path to the folder
img_folder=/sdcard/nh_files/img_folder
script_folder=/sdcard/nh_files/punkscripts
duckyscripts=/sdcard/nh_files/duckyscripts
busybox=/system/xbin/busybox
mnt=/data/local/nhsystem/kali-armhf
nh_files=/sdcard/nh_files/modules

init_env() {
    echo " [!] Initializing env.."
    if [ ! -d "$img_folder" ]; then
        mkdir -p $img_folder
    fi
    if [ ! -d "$script_folder" ]; then
        mkdir -p $script_folder
    fi
    [ $(busybox 2>&1 > /dev/null) ] && echo " [-] No busybox is installed !!" && exit 1
    [ ! "$(ls $mnt/sdcard)" ] && $busybox mount -o bind "/storage/emulated/0" "$mnt/sdcard"
    echo " [!] Finished Initializing."
}

# Languages supported: us, fr, de, es,sv, it, uk, ru, dk, no, pt, be
# Usage: HID $language "$write_your_duckyscript_here"
# But somehow you need to escape some 
QUICK_HID() {
cat << EOF > $nh_files/duckconvert.txt
$2
EOF
chroot $mnt /usr/bin/python $nh_files/duckhunter.py -l $1 $nh_files/duckconvert.txt /opt/duckout.sh
su -c sh $nh_files/duckout.sh
}
## Example below
# HID us "
# STRING ABCDEFGHIJKLMONPQRSTUVWXZ1234567890@#\$%&*-+()!\"':;/?,.~\`|{}_=[]\\<>
# TEXT Character you need to do escape are '\$', '\"', '\`', '\\'
# "

# Languages supported: us, fr, de, es,sv, it, uk, ru, dk, no, pt, be
# Usage: HID $language $path_to_your_ducky_script
LOAD_HID() {
cat $2 > $nh_files/duckconvert.txt
chroot $mnt /usr/bin/python $nh_files/duckhunter.py -l $1 $nh_files/duckconvert.txt /opt/duckout.sh
su -c sh $nh_files/duckout.sh
}
## Example below
# LOAD_HID us "/sdcard/nh_files/duckyscripts/helloworld"


# ============== #
# Function to check usb and adb status
# ============== #
check_usb_state() {
    usb_state=$(getprop sys.usb.state)
    echo $usb_state
}

check_adb_state() {
    adb_state=$(getprop sys.usb.ffs.ready)
    if [ "$adb_state" = "0" ] || [ "$adb_state" = "" ]; then
        adb_state="disabled"
        suffix=""
    else
        adb_state="enabled"
        suffix=",adb"
    fi
    echo $adb_state
}

check_img_folder() {
    echo $img_folder
}

check_script_folder() {
    echo $script_folder
}

check_mounted_img() {
    mounted_img="$(cat /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file)"
    if [ -z "$mounted_img" ]; then
        mounted_img=" No image file is being mounted"
    fi
    echo $mounted_img
}

# ============== #
# Function of printing main menu and submenu
# ============== #
## Main Menu ##
main_menu() {
    clear
    echo -n " [*] Current USB State: "
    check_usb_state
    echo -n " [*] Current ADB State: "
    check_adb_state
    echo -n " [*] Current folder for image files: "
    check_img_folder
    echo -n " [*] Current folder for payload scripts: "
    check_script_folder
    echo -n " [*] Current mounted image: "
    check_mounted_img
    echo "\n [*] Make Your Choice!!"
    echo
    echo "     0: Reset to default usb state based on your boot config"
    echo "     1: Switch usb state for Windows"
    echo "     2: Switch usb state for Mac OSX"
    echo "     3: Mount image file"
    echo "     4: Unmount image file"
    echo "     5: Run custom payload script"
    echo "     6: Switch USB debugging mode to ON/OFF"
    echo "    99: Exit"
    echo
    echo -n " [*] Option: "
# read user input
    read option
}
## 
menu_sub() {
    clear
    echo -n " [*] Current USB State: "
    check_usb_state
    echo -n " [*] Current ADB State: "
    check_adb_state
    echo -n " [*] Current folder for image files: "
    check_img_folder
    echo -n " [*] Current folder for payload scripts: "
    check_script_folder
    echo -n " [*] Current mounted image: "
    check_mounted_img
    echo "\n [*] Which usb state do you want to switch?"
    echo
    echo "     1: ${config[1]}"
    echo "     2: ${config[2]}"
    echo "     3: ${config[3]}"
    echo "     4: ${config[4]}"
    echo "     5: ${config[5]}"
    echo "     6: ${config[6]}" 
    echo "     7: ${config[7]}" 
    echo "    99: Back to previous menu"
    echo
    echo -n " [*] Option: "
# read user input
    read option
}

ending_banner() {
    echo " [+] Press Enter and see if change has been made."
    read
}

# ============== #
# Function of mounting and unmounting img files
# ============== #
mount_img() {
        echo "" > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file
        if [ "$(printf $1 | tail -c 4)" = ".iso" ]; then
            echo 1 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom
            echo 1 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro
        else
            echo 0 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom
            echo 0 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro
        fi
        echo "$1" > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file
}

unmount_img() {
    echo "" > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/file
    echo 0 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/cdrom
    echo 0 > /config/usb_gadget/g1/functions/mass_storage.0/lun.0/ro
}
# ============== #
# Program start here
# ============== #
#Start main loop until user chooses to exit
init_env
while true; do
    main_menu
    if [ "$option" = "99" ]; then
        echo
        echo " [!] Have a nice day!!"
        read
        exit
    # Reset sys.usb.config to default
    elif [ "$option" = "0" ]; then
        os=""
        $startusb$os${config[$option]}$suffix        
        ending_banner
        continue
    elif [ "$option" = 1 ]; then
        os="win,"
        config[3]="rndis"
        config[5]="rndis,hid"
        config[6]="rndis,mass_storage"
        config[7]="rndis,hid,mass_storage"
    elif [ "$option" = 2 ]; then
        os="mac,"
        config[3]="ecm"
        config[5]="ecm,hid"
        config[6]="ecm,mass_storage"
        config[7]="ecm,hid,mass_storage"
    elif [ "$option" = 3 ]; then
        #if [ -z "$(getprop sys.usb.state | grep 'mass_storage')" ]; then
        #   echo " [-] You are not in [MASS_STORAGE] mode, please check again"
        #    read
        #    continue
        #fi    
        if [ ! -d $img_folder ]; then
            echo " [-] Directory not existed! Please create one and place the image file there"
            echo " [!] Press Enter to continue"
            read
            continue
        fi
        file_list=($(IFS=$'\n' find $img_folder \( -name "*.img" -or -name "*.iso" \)))
        if [ -z $file_list ]; then
            echo " [-] No image files found in $img_folder"
            read
            continue
        else
            echo " [*] Please select the image file you want to mount:"
            i=0
            while [ i -lt ${#file_list[*]} ]; do
                IFS=$'\n' echo "  $((i+1)): ${file_list[(i)]}"
                i=$((i+1))
            done
            echo -n "\n Your option: "
            read option
            if [ "$option" -gt 0 ] && [ "$option" -lt $((i+2)) ] ; then
                mount_img "${file_list[(option-1)]}"
                echo " [+] Mounted, press Enter to continue."
                read
            else
                echo " [-] Invalid option!! Please input again."
                read
            fi
            continue
        fi
    elif [ "$option" = 4 ]; then     
        unmount_img
        echo " [+] Image file unmounted, press Enter to continue."
        read
        continue
    elif [ "$option" = 5 ]; then
        if [ ! -d $script_folder ]; then
            echo " [-] Directory not existed! Please create one and place the sh file there"
            echo " [!] Press Enter to continue"
            read
            continue
        fi
        file_list=($(IFS=$'\n' find $script_folder -name '*.sh' -not -name 'usb_army.sh'))
        if [ -z $file_list ]; then
            echo " [-] No shell script files found in $script_folder"
        else
            echo " [*] Please select the shell script file you want to run:"
            i=0
            while [ i -lt ${#file_list[*]} ]; do
                IFS=$'\n' echo "  $((i+1)): ${file_list[(i)]}"
                i=$((i+1))
            done
            echo -n "\n Your option: "
            read option
            if [ "$option" -gt 0 ] && [ "$option" -lt $((i+2)) ] ; then
                source "${file_list[(option-1)]}"
            else
                echo " [-] Invalid option!! Please input again."
                read
            fi
            continue
        fi
    elif [ "$option" = 6 ]; then
        if [ "$(settings get global adb_enabled)" = 1 ]; then
            echo " [+] Switch OFF the USB debugging."
            settings put global adb_enabled 0
        else
            echo " [+] Switch ON the USB debugging."
            settings put global adb_enabled 1
        fi
        read
        continue
    else
        echo " [-] Invalid option!! Please input again."
        read
        continue
    fi
    ## usb state menu
    menu_sub
    if [ "$option" -gt 0 ] && [ "$option" -lt 8 ] ; then
        final_cmd="$startusb$os${config[$option]}$suffix" ## construct the finalized command.
    elif [ "$option" = "99" ]; then
        continue
    else
        echo " [-] Invalid option!! Please input again."
        read
        continue
    fi
    ## execute the command.
    $final_cmd
    #check_usb_state
    #check_adb_state
    ending_banner
done



#!/system/bin/sh

# Make sure to use usb_army.sh to run all the scripts that include HID scripts.
## Vibrate two times
echo 300 > /sys/devices/virtual/timed_output/vibrator/enable
sleep 1

## Straight method: but need to escape special characters
# Usage: HID $language $duckyscript
QUICK_HID us "
STRING ABCDEFGHIJKLMONPQRSTUVWXZ1234567890@#\$%&*-+()!\"':;/?,.~\`|{}_=[]\\<>^
TEXT Character you need to do escape are '\$', '\"', '\`', '\\'
"

## Load the duckyscript file and exeute it.
LOAD_HID us "/sdcard/nh_files/duckyscripts/helloworld"

## Vibrate two times again
echo 300 > /sys/devices/virtual/timed_output/vibrator/enable
sleep 1

echo "finished"

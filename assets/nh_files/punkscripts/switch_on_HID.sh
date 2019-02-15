#!/bin/bash
first="$(ls /config/usb_gadget)"
second="$(ls /config/usb_gadget/$first/configs)"
linked_path="/config/usb_gadget/$first/configs/$second"
unlinked_path="/config/usb_gadget/$first/functions"
config_name="$(cat /config/usb_gadget/g1/configs/b.1/strings/0x409/configuration)"
UDC=$(getprop sys.usb.controller)
usb_state=$(getprop sys.usb.state)
#get enabled functions name
func_list=($(IFS=$'\n' find $linked_path -type l -exec readlink {} \; | cut -d'/' -f7,8))
count=0
while [ count -lt ${#func_list[@]} ]; do
    if [[ "${func_list[(count)]}" = "functions/" ]]; then
        func_list[(count)]=`echo "gsi.rndis"`
    else
        func_list[(count)]=`echo "${func_list[(count)]}" | sed "s/\(functions\/\)\(.*$\)/\2/g"`
    fi
    ((count++))
done
#here we need a new array without including the hid function
filter_func_list=()
for value in "${func_list[@]}"; do
    if [[ "$value" = "hid.0" ]] || [[ "$value" = "hid.1" ]]; then
        continue
    fi
    filter_func_list+=($value)
done
echo "Current enabled functions: ${func_list[@]}"
echo "Current enabled functions excluding HID: ${filter_func_list[@]}"
#unlink all possible enabled functions
count=${#func_list[@]}
while [ $count -gt 0 ]; do
    echo "Unlinking $linked_path/f"$count""
    unlink $linked_path/f"$count"
    ((count--))
done
#Now re-configure again
echo "$config_name" > $linked_path/strings/0x409/configuration
anti_count=${#filter_func_list[@]}
#symlink back the previous enabled funvtions
while [ count -lt ${#filter_func_list[@]} ]; do
    ((anti_count--))
    ((count++))
    echo "Linking $unlinked_path/${filter_func_list[$anti_count]} to $linked_path/f"$count""
    ln -s $unlinked_path/${filter_func_list[anti_count]} $linked_path/f"$count"
done
#then add symlink to hid
echo "Linking $unlinked_path/hid.0 to $linked_path/f"$((count+1))""
ln -s $unlinked_path/hid.0 $linked_path/f"$((count+1))"
echo "Linking $unlinked_path/hid.1 to $linked_path/f"$((count+2))""
ln -s $unlinked_path/hid.1 $linked_path/f"$((count+2))"
echo "$UDC" > /config/usb_gadget/$first/UDC
setprop sys.usb.state $usb_state
echo "Done."







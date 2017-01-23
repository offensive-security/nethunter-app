import argparse         # Handle arguments
import os               # To write hid comands to system
from keyseed import *   # The bytes to translate to keyboard codes

'''

Arguments for filetohid.py
python filetohid.py -f [inputfile] -l us
python filetohid.py -s "this is a string" -l us

This is a modified version of duckhunter.py.
Thanks to @byt3bl33d3r and @TheNain38 for help with original code.

'''
parser = argparse.ArgumentParser(description='Take input file or string and output it to hid')
parser.add_argument('-l', type=str, dest='layout', choices=['us', 'fr', 'de', 'es', 'sv', 'it', 'uk', 'ru', 'dk', 'no', 'pt', 'be'], help='Keyboard layout')
parser.add_argument('-f', '--file', type=str, help="Input file")
parser.add_argument('-s', '--string', type=str, help="Input string")
args = parser.parse_args()

# Variables for general keyboard commands, arguments
prefix = "echo "
suffix = " | /system/xbin/hid-keyboard /dev/hidg0 keyboard"
input_string = args.string
filename = args.file
language = args.layout

# If no language is specified, default to English
if not language:
    language = "us"


def do_file(filename, lang):
    try:
        os.system("/system/xbin/dos2unixdos2unix " + filename)
        f = open(filename, "r")
        for line in f:  # Read a line in the file
            for char in line:  # Read each character in that line
                #
                #  Start conversion here
                #
                if char != '\n':  # If the character is not a new line
                    if lang == "ru":  # If russian, set characters to russian
                        char = iso_ru[char]

                    line = dicts[lang+'_bin'].get(char)
                    if line is not None:
                        if isinstance(line, str):
                            os.system('%s%s%s\n' % (prefix, line.rstrip('\n').strip(), suffix))
                            #print('%s%s%s\n' % (prefix, line.rstrip('\n').strip(), suffix))
                        else:
                            for elem in line:
                                os.system('%s%s%s\n' % (prefix, elem.rstrip('\n').strip(), suffix))
                                #print('%s%s%s\n' % (prefix, elem.rstrip('\n').strip(), suffix))
                    else:
                        line = dicts[lang][char]
                        os.system('%s%s%s\n' % (prefix, line.rstrip('\n').strip(), suffix))
                        #os.system('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n') # releases key
                        os.system('sleep 0.03 \n') # Slow things down
                elif char == '\n':
                    os.system('echo enter | /system/xbin/hid-keyboard /dev/hidg0 keyboard\n')
    finally:
        f.close()


def do_string(string, lang):
    for char in string:
        #
        #  Start conversion here
        #
        if char != '\n' and char != '\r':  # If the character is not a new line
            if lang == "ru":  # If russian, set characters to russian
                char = iso_ru[char]

            line = dicts[lang+'_bin'].get(char)
            if line is not None:
                if isinstance(line, str):
                    os.system('%s%s%s\n' % (prefix, line.strip(), suffix))
                    print('%s%s%s\n' % (prefix, line.rstrip('\n').strip(), suffix))
                else:
                    for elem in line:
                        os.system('%s%s%s\n' % (prefix, elem.rstrip('\n').strip(), suffix))
                        #print('%s%s%s\n' % (prefix, elem.rstrip('\n').strip(), suffix))
            else:
                line = dicts[lang][char]
                os.system('%s%s%s\n' % (prefix, line.rstrip('\n').strip(), suffix))
                os.system('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n') # releases key
                os.system('sleep 0.03 \n') # Slow things down

#
# If input file is passed with -f, take that input file and read it
#
if args.file:
    do_file(filename, language)

#
# If string is passed with -s "this is a test string", print this to keyboard
#
if args.string:
    do_string(input_string, language)

#
# Stop a user from using both string and file.  Don't be crazy!
#
if args.string and args.file:
    print("Select either -f or -s, not both!")

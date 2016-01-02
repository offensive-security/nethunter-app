#! /usr/bin/env python

#Created by @binkybear and @byt3bl33d3r

import sys
import re
import os
from keyseed import *
import argparse
from decimal import Decimal #for conversion milliseconds -> seconds

parser = argparse.ArgumentParser(description='Converts USB rubber ducky scripts to a Nethunter format', epilog="Quack Quack")
parser.add_argument('-l', type=str, dest='layout', choices=['us', 'fr', 'de', 'es','sv', 'it', 'uk', 'ru', 'dk', 'no', 'pt', 'be'], help='Keyboard layout')
parser.add_argument('duckyscript', help='Ducky script to convert')
parser.add_argument('hunterscript', help='Output script')

args = parser.parse_args()

# Input file is argument / output file is output.txt
infile = open(args.duckyscript)
dest = open(args.hunterscript, 'w')
tmpfile = open("tmp.txt", "w")

def duckyRules (source):

    tmpfile = source

    for (k,v) in rules.items():
        regex = re.compile(k)
        tmpfile = regex.sub(v, tmpfile)

    return tmpfile

if __name__ == "__main__":

    rules = {
        r'ALT' : u'left-alt',
        r'GUI' : 'left-meta',
        r'WINDOWS' : 'left-meta',
        r'COMMAND' : 'left-meta',
        r'ALT' : 'left-alt',
        r'ALTGR' : 'right-alt',
        r'CONTROL' : 'left-ctrl',
        r'CTRL' : 'left-ctrl',
        r'SHIFT' : 'left-shift',
        r'MENU' : 'left-shift f10',
        r'APP' : 'escape',
        r'ESCAPE' : 'escape',
        r'ESC' : 'esc',
        r'END' : 'end',
        r'SPACE' : 'space',
        r'TAB' : 'tab',
        r'PRINTSCREEN' : 'print',
        r'ENTER' : 'enter',
        r'UPARROW' : 'up',
        r'UP' : 'up',
        r'DOWNARROW' : 'down',
        r'DOWN' : 'down',
        r'LEFTARROW' : 'left',
        r'LEFT' : 'left',
        r'RIGHTARROW' : 'right',
        r'RIGHT' : 'right',
        r'CAPSLOCK' : 'capslock',
        r'F1' : 'f1',
        r'F2' : 'f2',
        r'F3' : 'f3',
        r'F4' : 'f4',
        r'F5' : 'f5',
        r'F6' : 'f6',
        r'F7' : 'f7',
        r'F8' : 'f8',
        r'F9' : 'f9',
        r'F10' : 'f10',
        r'DELETE' : 'delete',
        r'INSERT' : 'insert',
        r'NUMLOCK' : 'numlock',
        r'PAGEUP' : 'pgup',
        r'PAGEDOWN' : 'pgdown',
        r'PRINTSCREEN' : 'print',
        r'BREAK' : 'pause',
        r'PAUSE' : 'pause',
        r'SCROLLLOCK' : 'scrolllock',
        r'BACKSPACE' : 'backspace',
        r'MOUSE MIDDLECLICK' : '--b3',
        r'MOUSE RIGHTCLICK' : '--b2',
        r'MOUSE LEFTCLICK' : '--b1',
        r'MOUSE leftCLICK' : '--b1', # Regex is lowering LEFT to left so we need to catch it.
        r'DEFAULT_DELAY' : '"defaultdelay', # We need to add this in between each line if it's set. For debugging
        r'REPEAT' : '"'}


    # For general keyboard commands
    prefix = "echo "
    suffix = " | hid-keyboard /dev/hidg0 keyboard"

    # For general mouse commands
    prefixmouse = "echo "
    suffixmouse = " | hid-keyboard /dev/hidg1 mouse"

    # Process input text
    prefixinput = 'echo -ne "'
    prefixoutput = '" > /dev/hidg0'

    with infile as text:
        new_text = duckyRules(text.read())
        infile.close()

    # Write regex to tmp file
    with tmpfile as result:
        result.write(new_text)
        tmpfile.close()

    src = open('tmp.txt', 'r')
    for line in src:

        if line.startswith('SLEEP ') or line.startswith('DELAY '):
            line = line.split()
            seconds = (Decimal(line[1]) / Decimal(1000)) % 60
            line[1] = str(seconds)
            line = ' '.join(line)
            dest.write('%s\n' % line.rstrip('\n').strip().lower())

        if line.startswith('DEFAULTDELAY ') or line.startswith('DEFAULT_DELAY '):
            line = line.split()
            seconds = (Decimal(line[1]) / Decimal(1000)) % 60
            line[1] = str(seconds)
            line = ' '.join(line)
            dest.write('%s\n' % line.rstrip('\n').strip())

        elif line.startswith('REM'):
            line = '#' + line.rstrip('\n')[4:]
            dest.write('%s\n' % line.strip())

        # Mouse commands
        elif line.startswith('--b'):
            dest.write('%s%s%s\n' % (prefixmouse, line.rstrip('\n').strip(), suffixmouse))

        elif line.startswith('MOUSE '):
            line = line[6:]
            dest.write('%s%s%s\n' % (prefixmouse, line.rstrip('\n').strip(), suffixmouse))

        # Shortcuts to Windows Command Line
        elif line.startswith('WINCMD'):
            dest.write('echo -ne "\\x08\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n') # Windows Key
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 1\n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x06\\x00\\x00\\x00\\x00" > /dev/hidg0\n') #C
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 0.1 \n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x10\\x00\\x00\\x00\\x00" > /dev/hidg0\n') #M
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 0.1 \n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x07\\x00\\x00\\x00\\x00" > /dev/hidg0\n') #D
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 1\n')
            dest.write('echo -ne "\\x10\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('echo -ne "\\x20\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 1\n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x28\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 3\n')

        elif line.startswith('WIN7CMD'):
            dest.write('echo -ne "\\x08\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n') # Windows Key
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 1\n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x06\\x00\\x00\\x00\\x00" > /dev/hidg0\n') #C
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 0.1 \n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x10\\x00\\x00\\x00\\x00" > /dev/hidg0\n') #M
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 0.1 \n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x07\\x00\\x00\\x00\\x00" > /dev/hidg0\n') #D
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 1\n')
            dest.write('echo left-ctrl left-shift return | hid-keyboard /dev/hidg0 keyboard\n')
            dest.write('sleep 2\n')
            if (args.layout == "us"):
                dest.write('echo left-alt y | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "fr"):
                dest.write('echo left-alt o | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "de"):
                dest.write('echo left-alt j | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "es"):
                dest.write('echo left-alt s | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "sv"):
                dest.write('echo left-alt j | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "it"):
                dest.write('echo left-alt s | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "uk"):
                dest.write('echo left-alt y | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "ru"):
                dest.write('echo left-alt d | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "dk"):
                dest.write('echo left-alt j | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "no"):
                dest.write('echo left-alt j | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "pt"):
                dest.write('echo left-alt s | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "be"):
                dest.write('echo left-alt o | hid-keyboard /dev/hidg0 keyboard\n')
            dest.write('sleep 3\n')

        elif line.startswith('WIN8CMD'):
            dest.write('sleep 1\n')
            dest.write('echo -ne "\\x08\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n') # Windows Key
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 1\n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x06\\x00\\x00\\x00\\x00" > /dev/hidg0\n') #C
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 0.1 \n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x10\\x00\\x00\\x00\\x00" > /dev/hidg0\n') #M
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 0.1 \n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x07\\x00\\x00\\x00\\x00" > /dev/hidg0\n') #D
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 0.1 \n')
            dest.write('echo -ne "\\x10\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 1\n')
            dest.write('echo -ne "\\x02\\x00\\x00\\x43\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 1\n')
            dest.write('echo -ne "\\x01\\x00\\x00\\x51\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 1\n')
            dest.write('echo -ne "\\x01\\x00\\x00\\x51\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 1\n')
            dest.write('echo -ne "\\x01\\x00\\x00\\x51\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 1\n')
            dest.write('echo -ne "\\x01\\x00\\x00\\x51\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 2\n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x28\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
            dest.write('sleep 2\n')
            if (args.layout == "us"):
                dest.write('echo left-alt y | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "fr"):
                dest.write('echo left-alt o | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "de"):
                dest.write('echo left-alt j | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "es"):
                dest.write('echo left-alt s | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "sv"):
                dest.write('echo left-alt j | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "it"):
                dest.write('echo left-alt s | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "uk"):
                dest.write('echo left-alt y | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "ru"):
                dest.write('echo left-alt d | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "dk"):
                dest.write('echo left-alt j | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "no"):
                dest.write('echo left-alt j | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "pt"):
                dest.write('echo left-alt s | hid-keyboard /dev/hidg0 keyboard\n')
            elif (args.layout == "be"):
                dest.write('echo left-alt o | hid-keyboard /dev/hidg0 keyboard\n')
            dest.write('sleep 3\n')

        # STRING to type and reads \n as ENTER
        elif line.startswith('STRING '):
            line = line[7:]
            for char in line:

                if char == '\n':  # Add enter if new line automagically
                    dest.write('echo enter | hid-keyboard /dev/hidg0 keyboard\n')
                else:
                    if args.layout == "ru":
                        char = iso_ru[char]

                    line = dicts[args.layout+'_bin'].get(char)
                    if line is not None:
                        if isinstance(line, str):
                            dest.write('%s%s%s\n' % (prefix, line.rstrip('\n').strip(), suffix))
                        else:
                            for elem in line:
                                dest.write('%s%s%s\n' % (prefix, elem.rstrip('\n').strip(), suffix))
                    else:
                        line = dicts[args.layout][char]
                        dest.write('%s%s%s\n' % (prefixinput, line.rstrip('\n').strip(), prefixoutput))
                        dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n') # releases key
                        dest.write('sleep 0.03 \n') # Slow things down

        # TEXT to type and NOT pass \n as ENTER.  Allows text to stay put.
        elif line.startswith('TEXT '):
            line = line.rstrip('\n')
            line = line[5:]
            for char in line:

                if char != '\n':
                    if args.layout == "ru":
                        char = iso_ru[char]

                    line = dicts[args.layout+'_bin'].get(char)
                    if line is not None:
                        if isinstance(line, str):
                            dest.write('%s%s%s\n' % (prefix, line.rstrip('\n').strip(), suffix))
                        else:
                            for elem in line:
                                dest.write('%s%s%s\n' % (prefix, elem.rstrip('\n').strip(), suffix))
                    else:
                        line = dicts[args.layout][char]
                        dest.write('%s%s%s\n' % (prefixinput, line.rstrip('\n').strip(), prefixoutput))
                        dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n') # releases key
                        dest.write('sleep 0.03 \n') # Slow things down
        else:
            if not line.startswith('SLEEP ') or line.startswith('DELAY '):
                dest.write('%s%s%s\n' % (prefix, line.rstrip('\n').strip(), suffix))

    src.close()
    dest.close()
    os.remove("tmp.txt")
    print "File saved to location: " + (args.hunterscript)

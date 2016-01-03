#! /usr/bin/env python

#Created by @binkybear and @byt3bl33d3r

import sys
import re
import os
from keyseed import *
import argparse
from decimal import Decimal #for conversion milliseconds -> seconds

parser = argparse.ArgumentParser(description='Converts USB rubber ducky scripts to a Nethunter format', epilog="Quack Quack")
parser.add_argument('-l', type=str, dest='layout', choices=['us', 'fr', 'de', 'es', 'sv', 'it', 'uk', 'ru', 'dk', 'no', 'pt', 'be'], help='Keyboard layout')
parser.add_argument('duckyscript', help='Ducky script to convert')
parser.add_argument('hunterscript', help='Output script')

args = parser.parse_args()

# Input file is argument / output file is output.txt
infile = open(args.duckyscript)
dest = open(args.hunterscript, 'w')
tmpfile = open("tmp.txt", "w")

def duckyRules (source):

    tmpfile = source

    for (k,v) in WINCMD_rules.items():
        regex = re.compile(k)
        tmpfile = regex.sub(v, tmpfile)

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
        r'F11' : 'f11',
        r'F12' : 'f12',
        r'F13' : 'f13',
        r'F14' : 'f14',
        r'F15' : 'f15',
        r'F16' : 'f16',
        r'F17' : 'f17',
        r'F18' : 'f18',
        r'F19' : 'f19',
        r'F20' : 'f20',
        r'F21' : 'f21',
        r'F22' : 'f22',
        r'F23' : 'f23',
        r'F24' : 'f24',
        r'DELETE' : 'delete',
        r'INSERT' : 'insert',
        r'NUMLOCK' : 'numlock',
        r'PAGEUP' : 'pgup',
        r'PAGEDOWN' : 'pgdown',
        r'PRINTSCREEN' : 'print',
        r'PRINTSCRN' : 'print',
        r'PRNTSCRN' : 'print',
        r'PRTSCN' : 'print',
        r'PRTSCR' : 'print',
        r'PRSC' : 'print',
        r'BREAK' : 'pause',
        r'PAUSE' : 'pause',
        r'SCROLLLOCK' : 'scrolllock',
        r'BACKSPACE' : 'backspace',
        r'BKSP' : 'backspace',
        r'MOUSE MIDDLECLICK' : '--b3',
        r'MOUSE RIGHTCLICK' : '--b2',
        r'MOUSE LEFTCLICK' : '--b1',
        r'MOUSE leftCLICK' : '--b1', # Regex is lowering LEFT to left so we need to catch it.
        r'SLEEP' : 'DELAY',
        r'DEFAULTDELAY' : 'DEFAULT_DELAY' # We need to add this in between each line if it's set. For debugging
    }

    # Shortcuts to Windows Command Line
    WINCMD_rules = {
        r'WINCMD' : 'GUI d\nDELAY 500\nGUI\nDELAY 1000\nTEXT cmd\nDELAY 1000\nENTER\nDELAY 3000',
        r'WINCMDUAC' : 'GUI d\nDELAY 500\nGUI\nDELAY 1000\nTEXT cmd\nDELAY 1000\nCTRL SHIFT ENTER\nDELAY 2000\nLEFTARROW\nENTER\nDELAY 3000'
    }


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

    line_backup = ''
    default_delay = ''
    src = open('tmp.txt', 'r')
    for line in src:

        repeat_counter = 0
        prevline = line_backup
        line_backup = line
        while True:
            if repeat_counter > 0:
                repeat_counter -= 1
                line = prevline

            if line == '' or line == '\n':
                break

            if line.startswith('DELAY '):
                line = line.rstrip('\n')[6:].strip()
                seconds = (Decimal(line) / Decimal(1000)) % 60
                line = str(seconds)
                dest.write('sleep %s\n' % line.strip().lower())

            else:

                if line.startswith('DEFAULT_DELAY'):
                    line = line.rstrip('\n')[13:].strip()
                    if line != '':
                        seconds = (Decimal(line) / Decimal(1000)) % 60
                        line = str(seconds)
                    default_delay = line
                    break

                elif line.startswith('REM'):
                    line = '#' + line.rstrip('\n')[3:]
                    dest.write('%s\n' % line.strip())

                # Mouse commands
                elif line.startswith('--b'):
                    dest.write('%s%s%s\n' % (prefixmouse, line.rstrip('\n').strip(), suffixmouse))

                elif line.startswith('MOUSE '):
                    line = line[6:]
                    dest.write('%s%s%s\n' % (prefixmouse, line.rstrip('\n').strip(), suffixmouse))

                # STRING to type and reads \n as ENTER
                elif line.startswith('STRING '):
                    line = line[7:]
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

                    dest.write('echo enter | hid-keyboard /dev/hidg0 keyboard\n') # Add enter

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

                elif line.startswith('REPEAT '):
                    line = line.rstrip('\n')[7:]
                    repeat_counter = int(line)
                    if repeat_counter <= 0:
                        break
                    else:
                        continue

                else:
                    dest.write('%s%s%s\n' % (prefix, line.rstrip('\n').strip(), suffix))

                if default_delay != '':
                    dest.write('sleep %s\n' % default_delay.strip().lower())

            if repeat_counter <= 0:
                break

    src.close()
    dest.close()
    os.remove("tmp.txt")
    print "File saved to location: " + (args.hunterscript)

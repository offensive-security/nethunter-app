#! /usr/bin/env python

#Created by @binkybear and @byt3bl33d3r
# Modified by @SimonPunk for OP5/5T
import sys
import re
import os
import shutil
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
        r'\bALT\b' : u'left-alt',
        r'\bGUI\b' : 'left-meta',
        r'\bWINDOWS\b' : 'left-meta',
        r'\bCOMMAND\b' : 'left-meta',
        r'\bALT\b' : 'left-alt',
        r'\bALTGR\b' : 'right-alt',
        r'\bCONTROL\b' : 'left-ctrl',
        r'\bCTRL\b' : 'left-ctrl',
        r'\bSHIFT\b' : 'left-shift',
        r'\bMENU\b' : 'left-shift f10',
        r'\bAPP\b' : 'escape',
        r'\bESCAPE\b' : 'escape',
        r'\bESC\b' : 'esc',
        r'\bEND\b' : 'end',
        r'\bSPACE\b' : 'space',
        r'\bTAB\b' : 'tab',
        r'\bPRINTSCREEN\b' : 'print',
        r'\bENTER\b' : 'enter',
        r'\bUPARROW\b' : 'up',
        r'\bUP\b' : 'up',
        r'\bDOWNARROW\b' : 'down',
        r'\bDOWN\b' : 'down',
        r'\bLEFTARROW\b' : 'left',
        r'\bLEFT\b' : 'left',
        r'\bRIGHTARROW\b' : 'right',
        r'\bRIGHT\b' : 'right',
        r'\bCAPSLOCK\b' : 'capslock',
        r'\bF1\b' : 'f1',
        r'\bF2\b' : 'f2',
        r'\bF3\b' : 'f3',
        r'\bF4\b' : 'f4',
        r'\bF5\b' : 'f5',
        r'\bF6\b' : 'f6',
        r'\bF7\b' : 'f7',
        r'\bF8\b' : 'f8',
        r'\bF9\b' : 'f9',
        r'\bF10\b' : 'f10',
        r'\bF11\b' : 'f11',
        r'\bF12\b' : 'f12',
        r'\bF13\b' : 'f13',
        r'\bF14\b' : 'f14',
        r'\bF15\b' : 'f15',
        r'\bF16\b' : 'f16',
        r'\bF17\b' : 'f17',
        r'\bF18\b' : 'f18',
        r'\bF19\b' : 'f19',
        r'\bF20\b' : 'f20',
        r'\bF21\b' : 'f21',
        r'\bF22\b' : 'f22',
        r'\bF23\b' : 'f23',
        r'\bF24\b' : 'f24',
        r'\bDELETE\b' : 'delete',
        r'\bINSERT\b' : 'insert',
        r'\bNUMLOCK\b' : 'numlock',
        r'\bPAGEUP\b' : 'pgup',
        r'\bPAGEDOWN\b' : 'pgdown',
        r'\bPRINTSCREEN\b' : 'print',
        r'\bPRINTSCRN\b' : 'print',
        r'\bPRNTSCRN\b' : 'print',
        r'\bPRTSCN\b' : 'print',
        r'\bPRTSCR\b' : 'print',
        r'\bPRSC\b' : 'print',
        r'\bBREAK\b' : 'pause',
        r'\bPAUSE\b' : 'pause',
        r'\bSCROLLLOCK\b' : 'scrolllock',
        r'\bBACKSPACE\b' : 'backspace',
        r'\bBKSP\b' : 'backspace',
        r'\bMOUSE MIDDLECLICK\b' : '--b3',
        r'\bMOUSE RIGHTCLICK\b' : '--b2',
        r'\bMOUSE LEFTCLICK\b' : '--b1',
        r'\bMOUSE leftCLICK\b' : '--b1', # Regex is lowering LEFT to left so we need to catch it.
        r'\bSLEEP\b' : 'DELAY',
        r'\bDEFAULTDELAY\b' : 'DEFAULT_DELAY' # We need to add this in between each line if it's set. For debugging
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

    prev_line = ''
    default_delay = ''
    src = open('tmp.txt', 'r')
    for source_line in src:

        if source_line == '' or source_line == '\n' or source_line.startswith('//'):
            continue

        repeat_counter = 1
        while repeat_counter > 0:

            repeat_counter -= 1
            line = source_line

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

                            #                            line = dicts[args.layout+'_bin'].get(char)
                            line = dicts[args.layout][char]
                            if line is not None:
                                if isinstance(line, str):
                                    dest.write('%s%s%s\n' % (prefixinput, line.rstrip('\n').strip(), prefixoutput))
                                    dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
                                else:
                                    for elem in line:
                                        dest.write('%s%s%s\n' % (prefixinput, elem.rstrip('\n').strip(), prefixoutput))
                                        dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
                            else:
                                line = dicts[args.layout][char]
                                dest.write('%s%s%s\n' % (prefixinput, line.rstrip('\n').strip(), prefixoutput))
                                dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n') # releases key
                            #  dest.write('sleep 0.03 \n') # Slow things down

                    dest.write('echo enter | hid-keyboard /dev/hidg0 keyboard\n') # Add enter

                # TEXT to type and NOT pass \n as ENTER.  Allows text to stay put.
                elif line.startswith('TEXT '):
                    line = line.rstrip('\n')
                    line = line[5:]
                    for char in line:

                        if char != '\n':
                            if args.layout == "ru":
                                char = iso_ru[char]

                            #                            line = dicts[args.layout+'_bin'].get(char)
                            line = dicts[args.layout][char]
                            if line is not None:
                                if isinstance(line, str):
                                    dest.write('%s%s%s\n' % (prefixinput, line.rstrip('\n').strip(), prefixoutput))
                                    dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
                                else:
                                    for elem in line:
                                        dest.write('%s%s%s\n' % (prefixinput, elem.rstrip('\n').strip(), prefixoutput))
                                        dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n')
                            else:
                                line = dicts[args.layout][char]
                                dest.write('%s%s%s\n' % (prefixinput, line.rstrip('\n').strip(), prefixoutput))
                                dest.write('echo -ne "\\x00\\x00\\x00\\x00\\x00\\x00\\x00\\x00" > /dev/hidg0\n') # releases key
                        #      dest.write('sleep 0.03 \n') # Slow things down

                elif line.startswith('REPEAT '):
                    line = line.rstrip('\n')[7:]
                    repeat_counter = int(line)
                    source_line = prev_line

                    if source_line == '':
                        break

                    continue

                else:
                    dest.write('%s%s%s\n' % (prefix, line.rstrip('\n').strip(), suffix))

                if default_delay != '':
                    dest.write('sleep %s\n' % default_delay.strip().lower())

        prev_line = source_line

    src.close()
    dest.close()
    os.remove("tmp.txt")
    shutil.copy2('/opt/duckout.sh', '/sdcard/nh_files/modules/duckout.sh')
    # print "File saved to location: " + (args.hunterscrip
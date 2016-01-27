#!/usr/bin/env python

# Credits for regular expression @jsthyer
# from: https://bitbucket.org/jsthyer/psploitgen

import re


def fix_payload(p):
    out = ''
    p = re.sub(r'\r', '', p)
    for line in p.split('\n'):
        line = re.sub(r'^\$buf\s\+=\s', '', line)
        out = '%s,%s' % (out, line)
    return re.sub(r'^,\[Byte\[\]\]\s{0,1}\$buf\s{0,1}=\s{0,1}', '', out[:-1])

try:
    f = open('/tmp/pwrshell_string', 'r')
    output = f.read()
    f.close()

    payload = fix_payload(output)
    newpayload = "@(%s)" % payload
    print newpayload

except IOError:
    print('Error opening file')

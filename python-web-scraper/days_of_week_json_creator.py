import json

def dec_to_bin(num):
    return '{0:b}'.format(num).zfill(7)

key_val_dict = {}

for i in range(0, 128):
    bin = str(dec_to_bin(i))[::-1]
    key = ''
    value = [False, False, False, False, False, False, False]

    if bin[0] == '1':
        key += 'M'
        value[0] = True

    if bin[1] == '1':
        key += 'T'
        value[1] = True

    if bin[2] == '1':
        key += 'W'
        value[2] = True

    if bin[3] == '1':
        key += 'Th'
        value[3] = True

    if bin[4] == '1':
        key += 'F'
        value[4] = True

    if bin[5] == '1':
        key += 'S'
        value[5] = True

    if bin[6] == '1':
        key += 'U'
        value[6] = True

    key_val_dict[key] = value

print(str(json.dumps(key_val_dict)).replace('], ', '],\n'))
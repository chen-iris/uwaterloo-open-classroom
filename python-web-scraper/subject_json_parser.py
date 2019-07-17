import json

with open('./subjects.json') as file:
    file_string = str(file.read())
    subject_json = json.loads(file_string)
    data = subject_json['data']
    subjects = [item['subject'] for item in data]

    print('<?xml version="1.0" encoding="utf-8"?>')
    print('<resources>')
    print('\t<string-array name="course_subjects">')

    for subject in subjects:
        print('\t\t<item>{}</item>'.format(subject))
    print('\t</string-array>')
    print('</resources>')

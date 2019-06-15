import os

from selenium import webdriver
from selenium.webdriver.support.ui import Select
from bs4 import BeautifulSoup

TERM_NUMBER = '1195'
CLASS_SCHEDULE_QUERY_URL = 'http://www.adm.uwaterloo.ca/infocour/CIR/SA/under.html'


def retrieve_html_pages():
    """Retrieves the HTML web pages of the class schedules for each subject."""

    # Start a Chrome browser
    driver = webdriver.Chrome()
    driver.get(CLASS_SCHEDULE_QUERY_URL)

    # Select the term
    term_selection = driver.find_element_by_name('sess')
    term_selector = Select(term_selection)
    term_selector.select_by_value(TERM_NUMBER)

    # Get a list of all the subject names
    subject_selection = driver.find_element_by_name('subject')
    subject_options = [e for e in subject_selection.find_elements_by_tag_name("option")]
    subject_names = [so.get_attribute('value') for so in subject_options]

    # Retrieve the html page displaying the class schedule for every subject
    for name in subject_names:
        # Select the subject
        subject_selection = driver.find_element_by_name('subject')
        subject_selector = Select(subject_selection)
        subject_selector.select_by_value(name)

        # Click the search button
        search_button = driver.find_element_by_xpath('//input[@value="Search!"]')
        search_button.click()

        # Determine the file name to store the html page source
        filename = "./class_schedules/{}_schedule.html".format(name.lower())

        # Open and write the html page source to that file
        with open(filename, "w") as file:
            file.write(driver.page_source)

        # Return the search page
        driver.back()


def retrieve_class_schedules():
    room_schedules = []

    for filename in os.listdir('./class_schedules'):
        with open('./class_schedules/{}'.format(filename), 'r') as html_file:

            soup = BeautifulSoup(html_file, 'html.parser')
            course_tables = soup.select('html body table tbody tr td table')

            for course_table in course_tables:

                table_headers = course_table.select('tbody tr th')
                table_header_text = [th.getText() for th in table_headers]
                room_index = table_header_text.index('Bldg Room')
                time_index = table_header_text.index('Time Days/Date')

                table_rows = course_table.select('tbody tr')

                course_section_rows = list(filter(
                    lambda r: is_course_section_row(r),
                    [row.select('td') for row in table_rows]))

                room_schedules += [{
                    'room': row[room_index].getText().strip(),
                    'time': row[time_index].getText().strip()
                } for row in course_section_rows]

    for rs in room_schedules:
        print(rs)

    print(len(room_schedules))
    # 2370 rooms


def is_course_section_row(row_cols):
    if len(row_cols) == 0:
        return False

    class_number = row_cols[0].getText().strip()

    if not class_number.isdigit():
        return False

    return 1000 <= int(class_number) <= 9999


def main(refresh_html_files=False):
    if refresh_html_files:
        retrieve_html_pages()

    retrieve_class_schedules()

if __name__ == '__main__':
    main()

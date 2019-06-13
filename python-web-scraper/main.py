from selenium import webdriver
from selenium.webdriver.support.ui import Select
from selenium.webdriver.chrome.options import Options


def get_html(url):
    driver = webdriver.Chrome()
    driver.get(url)
    html = driver.page_source

    term_selection = driver.find_element_by_name('sess')
    subject_selection = driver.find_element_by_name('subject')
    select = Select(term_selection)
    select.select_by_value('1191')

    print(term_selection.get_attribute('innerHTML'))
    print(subject_selection.get_attribute('innerHTML'))
    input("quit")
    return html


def main():
    chrome_options = webdriver.ChromeOptions()
    chrome_options.add_experimental_option("detach", True)
    get_html('http://www.adm.uwaterloo.ca/infocour/CIR/SA/under.html')


if __name__ == '__main__':
    main()

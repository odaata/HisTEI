import argparse
import codecs
import csv
import cStringIO
import os
import re

from lxml import etree

TEI = 'http://www.tei-c.org/ns/1.0'
XML = 'http://www.w3.org/XML/1998/namespace'
NS = {'ns': TEI, 'x': XML}
TAG = '{{{0}}}{1}'
FORMAT = {'abbr': u'_{}_', 'expan': u'[{}]', 'supplied': u'{{{}}}', 'unclear': u'!{}!'}


def has_language_switch(element, current_lang=None):
    """
    Checks whether there is a switch in languages within a <gloss> element.
    Checks child elements recursively.
    """
    result = False
    lang = element.get(TAG.format(XML, 'lang'))
    if current_lang is None and lang is not None:
        current_lang = lang

    # If the current language is equal to the specified language
    # (or no language is specified), continue recursively.
    if lang is None or current_lang == lang:
        for c in element.iterchildren():
            result |= has_language_switch(c, current_lang)
    else:  # if current_lang != lang
        result = True
    return result


def get_text(element):
    """
    Retrieves the text for an element (<term> or <gloss>), with special actions for certain tags.
    Is similar to element.itertext().
    """
    result = ''
    simple_tag = element.tag[len(TEI) + 2:]
    if simple_tag in ['gap']:
        result += '(GAP)'
    elif simple_tag in ['del', 'note']:
        pass
    elif simple_tag in ['pb', 'lb', 'handshift']:
        if element.get(TAG.format(TEI, 'break')) == 'yes':
            result += ' '
    elif element.text:
        if simple_tag in FORMAT:
            result += FORMAT.get(simple_tag).format(element.text)
        else:
            result += element.text

    for c in element.iterchildren():
        if simple_tag in FORMAT and not element.text:
            result += FORMAT.get(simple_tag).format(get_text(c))
        else:
            result += get_text(c)
        if c.tail:
            result += c.tail
    return result


def get_sentence_ids(gloss):
    """
    Returns the id attribute of the child <s> elements of a <gloss> element.
    """
    ids = []
    for s in gloss.findall('./ns:s', namespaces=NS):
        ids.append(s.get(TAG.format(XML, 'id')))
    return ids


def extract_glosses(filename):
    """
    Extract all <gloss> elements and corresponding <sentence> and <term> elements into a .csv-file.
    """
    with open(os.path.splitext(filename)[0] + '_out.csv', 'wb') as f:
        f.write(u'\uFEFF'.encode('utf-8'))  # the UTF-8 BOM to hint Excel we are using that...
        csv_writer = UnicodeWriter(f, delimiter=';')
        csv_writer.writerow(['gloss_id', 'gloss_lang', 'gloss_text',
                             'sentence_ids', 'has_switch',
                             'term_id', 'term_lang', 'term_text'])

        tree = etree.parse(filename)
        for n, gloss in enumerate(tree.findall('//ns:gloss', namespaces=NS)):
            gloss_id = str(n + 1)
            gloss_text = re.sub(r'\s+', ' ', get_text(gloss)).strip()
            gloss_lang = gloss.get(TAG.format(XML, 'lang'), 'No language specified')
            sentence_ids = ','.join(get_sentence_ids(gloss))
            has_switch = 'yes' if has_language_switch(gloss) else 'no'
            target = gloss.get('target')
            if target:
                target = target[1:]
                terms = tree.findall('//ns:term[@x:id="' + target + '"]', namespaces=NS)

                if len(terms) > 1:
                    print 'Warning: multiple terms specified for ' + target

                if terms:
                    term = terms[0]
                    term_id = term.get(TAG.format(XML, 'id'))
                    term_lang = term.get(TAG.format(XML, 'lang'), 'No language specified')
                    term_text = re.sub(r'\s+', ' ', get_text(term)).strip()
                else:
                    term_id = target
                    term_lang = 'Not found!'
                    term_text = 'Not found!'
            else:
                term_id = 'No target specified'
                term_lang = ''
                term_text = ''

            csv_writer.writerow([gloss_id, gloss_lang, gloss_text,
                                 sentence_ids, has_switch,
                                 term_id, term_lang, term_text])


class UnicodeWriter:
    """
    A CSV writer which will write rows to CSV file "f",
    which is encoded in the given encoding.
    Copied from https://docs.python.org/2/library/csv.html#examples
    """

    def __init__(self, f, dialect=csv.excel, encoding='utf-8', **kwds):
        # Redirect output to a queue
        self.queue = cStringIO.StringIO()
        self.writer = csv.writer(self.queue, dialect=dialect, **kwds)
        self.stream = f
        self.encoder = codecs.getincrementalencoder(encoding)()

    def writerow(self, row):
        self.writer.writerow([s.encode('utf-8') for s in row])
        # Fetch UTF-8 output from the queue ...
        data = self.queue.getvalue()
        data = data.decode('utf-8')
        # ... and reencode it into the target encoding
        data = self.encoder.encode(data)
        # write to the target stream
        self.stream.write(data)
        # empty queue
        self.queue.truncate(0)

    def writerows(self, rows):
        for row in rows:
            self.writerow(row)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Extracts glosses from a TEI-XML file.')
    parser.add_argument('files', metavar='f', type=str, nargs='+', help='to be processed files')

    args = parser.parse_args()

    for file in args.files:
        extract_glosses(file)

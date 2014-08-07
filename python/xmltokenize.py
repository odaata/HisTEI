from lxml import etree
import re
import glob
import nltk.tokenize.punkt 
import pickle
import os

NS = '{http://www.tei-c.org/ns/1.0}'
NSmap = {'ns': 'http://www.tei-c.org/ns/1.0'}
STRIP = ['lb', 'handShift', 'pc', 'expan', 'date', 'abbr', 'unclear', 'del', 'gap', 'add', 'pb']

ABBR = set()

# Convert .xml to .txt-files
for fn in glob.glob('*.xml'):
    with open(fn, 'r') as input_file:  
        # Parse the XML  
        root = etree.parse(input_file)
            
        # Gather all abbreviations from the texts, use them during the training phase
        for abbr in root.xpath('./ns:text//ns:abbr', namespaces = NSmap): 
            # Convert abbreviations to UTF-8 and replace enters and extra spaces
            abbreviation = re.sub('\s\s+', ' ', abbr.text.encode('UTF-8').replace('\n', ' '))
            # Abbreviations must be lower-case
            abbreviation = abbreviation.lower()
            # Abbreviations should not have a dot at then end
            if abbreviation [-1:] == '.': 
                abbreviation = abbreviation [:-1]
            # Add to the set
            ABBR.add(abbreviation)
        
        # Add numbers from 0 to 2000 to the abbreviation set; training set will sure not contain all
        for n in xrange(0, 2000):
            ABBR.add(str(n))
        
        # Open the output file
        with open(fn[:-4] + '-strip.txt', 'wb') as out:
            # Gather all the text from p tags
            for p in root.xpath('./ns:text//ns:p', namespaces = NSmap): 
                # Strip all the tags from within the p tags
                for s in STRIP: 
                    etree.strip_tags(p, NS + s)
                    
                # Convert abbreviations to UTF-8 and replace enters and extra spaces
                text = re.sub('\s\s+', ' ', p.text.encode('UTF-8'))
                text = text.replace('\n', ' ').replace('=', '').strip() 
                out.write(text + '\n\n')

# Create tokenizer, use the abbreviations collected above
punkt_param = nltk.tokenize.punkt.PunktParameters()
punkt_param.abbrev_types = ABBR 
tokenizer = nltk.tokenize.punkt.PunktSentenceTokenizer(punkt_param) 

# Read in training corpus
for fn in glob.glob('91-strip.txt'): # 91.xml as training file
    with open(fn, 'r') as input_file:
        text = input_file.read()
        tokenizer.train(text) 

# Dump pickled tokenizer 
with open('dutch.pickle', 'wb') as out: 
    pickle.dump(tokenizer, out) 
    
# Read in test corpus
for fn in glob.glob('*-strip.txt'):
    with open(fn, 'r') as input_file:
        text = input_file.read()
        
        # Load sentence detector
        #sent_detector = nltk.data.load('file://' + os.getcwd() + '/dutch.pickle') 	# Linux
        sent_detector = nltk.data.load('file:' + os.getcwd() + '\dutch.pickle')		# Windows
        sentences = sent_detector.tokenize(text, punkt_param)
        
        # Write output to file
        with open(fn[:-10] + '-out.txt', 'wb') as out: 
            out.write('\n\n'.join(sentences))


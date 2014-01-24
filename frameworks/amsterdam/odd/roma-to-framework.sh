#!/bin/bash

# Params for this script:
#	$1 - path to odd document to be converted to schemas
#	$2 - path to framework/schema directory

# Typical usage for roma2 program
# roma2 ~/Downloads/amsterdam_list_place.xml

ODD=${1:-~/Documents/Dissertation/Corpus/oxygen/frameworks/amsterdam/odd/amsterdam_list_place.odd}
SCHEMAS=${2:-~/Documents/Dissertation/Corpus/oxygen/frameworks/amsterdam/schema/}

roma2 "$ODD"
cp ~/RomaResults/*.rng "$SCHEMAS"

# roma2 "$1"
# cp ~/RomaResults/*.rng "$2"

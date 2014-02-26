#!/bin/bash

# Params for this script:
#	$1 - path to odd document to be converted to schemas
#	$2 - path to framework/schema directory

# Typical usage for roma2 program
# roma2 ~/Downloads/amsterdam_list_place.xml

#ODD=${1:-/opt/oxygen/frameworks/emst/odd/emst.odd}
ODD=${1:-~/Downloads/emst.xml}
SCHEMAS=${2:-/opt/oxygen/frameworks/emst/schema/}

roma2 "$ODD"
cp ~/RomaResults/*.rng "$SCHEMAS"

# roma2 "$1"
# cp ~/RomaResults/*.rng "$2"

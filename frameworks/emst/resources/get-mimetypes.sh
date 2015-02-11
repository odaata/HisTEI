#!/bin/bash

for f in "$1"*
do
    echo "$(basename "$f")" $'\t' $(file -bi "$f")
done

#> "/home/mike/Downloads/EMST_output/mimetypes.txt"
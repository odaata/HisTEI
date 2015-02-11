#!/bin/bash
# converting all files in a dir to utf8

for f in "$1"*
do
    echo -e "\nConverting $f"
    CHARSET="$( file -bi "$f"|awk -F "=" '{print $2}')"

    if [ "$CHARSET" != utf-8 ]; then
        if [ "$CHARSET" == "unknown-8bit" ]; then
            FROM=windows-1252
        else
            FROM="$CHARSET"
        fi
        iconv -f "$FROM" -t utf8 "$f" -o "$2""$(basename "$f")"
    fi
done

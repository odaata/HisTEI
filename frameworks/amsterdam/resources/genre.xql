xquery version "3.0";

(:import module namespace teix="http://cohd.info/xquery/tei" at "tei.xqm";:)

declare default element namespace "http://www.tei-c.org/ns/1.0";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
(:declare namespace fn="http://www.w3.org/2005/xpath-functions";:)

element taxonomy {
    attribute xml:id { "EMST_GENRES" },
    
    for $line in unparsed-text-lines("genre.txt")
    let $parts := tokenize($line, " - ")
    let $key := replace(normalize-space(replace($parts[1], "[()/]", " ")), " ", "_")
    let $desc := normalize-space(if (count($parts) gt 1) then $parts[2] else $parts[1])
    return
        if (string-length(($key)) gt 0) then
            element category {      
                attribute xml:id { $key },
                element catDesc { $desc }
            }     
        else
            ()
}
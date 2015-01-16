xquery version "3.0";

import module namespace functx="http://www.functx.com" at "functx.xql";

declare default element namespace "http://www.tei-c.org/ns/1.0";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace uuid = "java:java.util.UUID";

declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";
declare option output:omit-xml-declaration "no";
declare option output:indent "yes";

(: TEI-specific element names and their use in our corpus:)
declare variable $contentNames := ("p", "head", "dateline", "signed", "salute", 
                                    "byline", "argument", "epigraph", "trailer");
declare variable $breakNames := ( "cb", "gb", "lb", "milestone", "pb");
declare variable $milestoneNames := ($breakNames, "handShift");
declare variable $subWordToSplitNames := ("expan", "supplied", "unclear");
declare variable $subWordNeverTokenizedNames := ("abbr", "num", "measure");
declare variable $subWordNames := ($subWordToSplitNames, $subWordNeverTokenizedNames, "gap");
declare variable $annotationNames := ("w", "pc");
declare variable $editNames := ("add", "del", "hi");

declare function local:replace-content($element as element(), $newContent, 
                                           $newAttributes as attribute()*) as element() {
    element { local-name($element) } {
        if (exists($newAttributes)) then $newAttributes else $element/@*,
        $newContent
    }
};

declare function local:replace-content($element as element(), $newContent) as element() {
    local:replace-content($element, $newContent, ())
};

(: $type can be "anywhere", "starts", "ends", "all" nothing defaults to "anywhere" :)
declare function local:contains-ws($string as xs:string?, $type as xs:string?) as xs:boolean {
    let $regex :=
        switch ($type)
        case "starts" return "^\s"
        case "ends" return "\s$"
        case "all" return "^\s+$"
        default return "\s"
    return
        matches($string, $regex)
};

declare function local:contains-ws($string as xs:string?) as xs:boolean {
    local:contains-ws($string, ())
};

let $lalpTrans := unparsed-text("/home/mike/Dropbox/IrEPL/IrEPL_transcriptions/plain text/_18XX_XX_XX_Anonymous_XX.txt")
let $lines :=
    for $line in tokenize($lalpTrans, "[\r\n]+")
    return
        $line
return
    element lines {
        for $line in $lines
        return
            element line { $line }
    }




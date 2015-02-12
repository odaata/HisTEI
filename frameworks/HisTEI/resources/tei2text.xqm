xquery version "3.0";

(:~
 : A set of helper functions to transform TEI data to other formats (only text for now)
 :)
module namespace txt="http://cohd.info/xquery/tei2text";

import module namespace functx="http://www.functx.com" at "functx.xql";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace tei = "http://www.tei-c.org/ns/1.0";

declare variable $txt:contextLength := 128;

declare variable $txt:contentElements := ("p", "opener", "closer", "postscript", "gloss");
declare variable $txt:milestoneElements := ("pb", "lb", "handShift");
declare variable $txt:removedElements := ("del", "note");
declare variable $txt:replacedElements := map{ "gap" := "(GAP)" };
declare variable $txt:wrappedElements := map{
    "abbr" := ("_", "_"),
    "expan" := ("[", "]"),
    "supplied" := ("{", "}"),
    "unclear" := ("!", "!")
};

(: Functions for TEI fields :)

declare variable $txt:CERTAINTY := map { 
    "unknown" := "",
    "low" := "*",
    "medium" := "",
    "high" := "^"
};

declare function txt:category($category as element(tei:category)) as xs:string+ {
    (
       string(replace($category/@xml:id, "_", " ")), 
       string(normalize-space($category/tei:catDesc/text()))
    )
};

declare function txt:place($place as element(tei:place)) as xs:string {
    string(normalize-space($place/tei:placeName/text()))
};

declare function txt:org($org as element(tei:org)) as xs:string {
    string(normalize-space($org/tei:orgName/text()))
};

declare function txt:person($person as element(tei:person)) as xs:string {
    let $name := txt:name-info($person/tei:persName)
    let $birth := txt:year-info($person/tei:birth)
    let $death := txt:year-info($person/tei:death)
    let $dates := 
        if ($birth("year") or $death("year")) then 
            concat("(", $birth("year"), "-", $death("year"), ")")
        else 
            ""
    return
        string-join(($name("forename"), $name("surname"), $dates), " ")
};

declare function txt:name-info($persName as element(tei:persName)?) as map(xs:string, xs:string)? {
    
    if ($persName) then
        let $role := string-join(for $name in $persName/tei:roleName order by $name/@sort return $name/text(), " ")
        let $forename := string-join(for $name in $persName/tei:forename order by $name/@sort return $name/text(), " ")
        let $maiden := string-join(for $name in $persName/*[@type eq "maiden"] order by $name/@sort return $name/text(), " ")
        let $surname := string-join(
            for $name in $persName/*[local-name(.) = ("surname", "nameLink", "genName") 
                and not(exists(./@type) and ./@type eq "maiden")] 
            order by $name/@sort 
            return $name/text(), " ")
            
        return
            map:new((
                if ($role) then map:entry("role", $role) else (),
                if ($forename) then map:entry("forename", $forename) else (),
                if ($maiden) then map:entry("maiden", $maiden) else (),
                if ($surname) then map:entry("surname", $surname) else ()
            ))
    else
        ()
};

declare function txt:format-year($datable as element(), $single-estimates as xs:boolean) as xs:string {
    let $when := txt:year($datable/@when)
    let $notBefore := txt:year($datable/@notBefore)
    let $notAfter := txt:year($datable/@notAfter)
    let $from := txt:year($datable/@from)
    let $to := txt:year($datable/@to)
    let $cert := if ($datable/@cert) then $txt:CERTAINTY($datable/@cert) else ""
    return
    switch(true())
        case exists($when) return concat($when, $cert)
        
        case exists($notBefore) and exists($notAfter) return
            if ($single-estimates) then 
                concat((($notAfter - $notBefore) idiv 2) + $notBefore, $cert)
            else
                concat($notBefore, "-", $notAfter, $cert, " nb-na")
        
        case exists($notBefore) return concat($notBefore, $cert, " nb")
        
        case exists($notAfter) return concat($notAfter, $cert, " na")
        
        case exists($from) or exists($to) return
            concat($from, "-", $to, $cert, " dur")
            
        default return ""
};

declare function txt:format-year($datable as element()) as xs:string {
    txt:format-year($datable, true())
};

declare function txt:year-info($datable as element()?, $single-estimates as xs:boolean) as map(xs:string, item()) {
    if (empty($datable)) then
        map:new()
    else
        let $when := txt:year($datable/@when)
        let $notBefore := txt:year($datable/@notBefore)
        let $notAfter := txt:year($datable/@notAfter)
        let $from := txt:year($datable/@from)
        let $to := txt:year($datable/@to)
        let $cert := string($datable/@cert)
        return
            switch(true())
            case exists($when) return 
                map { "year" := $when, "cert" := $cert }
            
            case exists($notBefore) and exists($notAfter) return
                if ($single-estimates) then 
                    map { "year" := (($notAfter - $notBefore) idiv 2) + $notBefore, "cert" := "nb/na" }
                else
                    map { "year" := concat($notBefore, "-", $notAfter), "cert" := "nb-na" }
            
            case exists($notBefore) return 
                map { "year" := $notBefore, "cert" := "nb" }
            
            case exists($notAfter) return 
                map { "year" := $notAfter, "cert" := "na" }
            
            case exists($from) or exists($to) return
                map { "year" := concat($from, "-", $to), "cert" := $cert }
                
            default return 
                map:new()
};

declare function txt:year-info($datable as element()?) as map(xs:string, item())? {
    txt:year-info($datable, true())
};

declare function txt:year($date as xs:string?) as xs:integer? {
    if (matches($date, "^\d{4}")) then
        xs:integer(substring($date, 1, 4))
    else
        ()
};

(: Functions for processing mixed-content text nodes :)

declare function txt:toText($textNodes as node()*) as xs:string {
    if (exists($textNodes)) then
        normalize-space(
            string-join(
                txt:transformNodes($textNodes)
            )
        )
    else
        ""
};

declare function txt:transformNodes($nodes as node()*) as xs:string* {
    for $node in $nodes
    return
        typeswitch ($node)
        case text() return
            $node
        case element() return
            let $name := local-name($node)
            return
                if ($name = $txt:milestoneElements) then
                    if ($node/@break eq "no") then "" else " "
                
                else if ($name eq "seg" and $node/@function eq "formulaic") then 
                    let $wrappedNodes := txt:transformNodes($node/node())
                    return
                        if (exists($wrappedNodes)) then
                            string-join(("^", $wrappedNodes, "^"))
                        else
                            ""
                
                else if ($name = $txt:removedElements) then ""
                
                else if (map:contains($txt:replacedElements, $name)) then $txt:replacedElements($name)
                
                else if (map:contains($txt:wrappedElements, $name)) then
                    let $wrappers := $txt:wrappedElements($name)
                    let $wrappedNodes := txt:transformNodes($node/node())
                    return
                        if (exists($wrappedNodes)) then
                            string-join(($wrappers[1], $wrappedNodes, $wrappers[2]))
                        else
                            ""
                else
                    txt:transformNodes($node/node())
        default return 
            ()
};

declare function txt:outerNodes($elem as element()?, $previous as xs:boolean, $topElem as element()?) as node()* {
    if (empty($elem) or (exists($topElem) and ($topElem eq $elem))) then
        ()
    else
        let $parent := $elem/parent::*
        return
            if ($previous) then 
                (txt:outerNodes($parent, $previous, $topElem), $elem/preceding-sibling::node()) 
            else
                ($elem/following-sibling::node(), txt:outerNodes($parent, $previous, $topElem))
};

declare function txt:cutToWord($start as xs:boolean, $input as xs:string?, $outputLength as xs:integer?) as xs:string? {
    let $continuedSymbol := "&#8230;"
    let $outputLength := if (empty($outputLength) or $outputLength < 1) then $txt:contextLength else $outputLength
    let $input := functx:trim($input)
    let $inputLength := string-length($input)
    return
        if (empty($input) or $inputLength <= $outputLength) then
            $input
        else
            let $charPos := if ($start) then $outputLength else $inputLength - $outputLength
            let $output := 
                if ($start) then 
                    substring($input, 1, $outputLength + 1) 
                else 
                    substring($input, $inputLength - ($outputLength + 1))
            return
                if ($start) then
                    replace($output, "\s+(\S*)$", "") || $continuedSymbol
                else
                    $continuedSymbol || replace($output, "^(\S*)\s+", "")
};

declare function txt:cutToWord($start as xs:boolean, $input as xs:string?) as xs:string? {
    txt:cutToWord($start, $input, ())
};

declare function txt:get-content-element($element as element()?) as element()? {
    if (exists($element)) then
        $element/ancestor::*[local-name() = $txt:contentElements][1]
    else
        ()
};

declare function txt:context($elem as element()) as element(context) {
    let $contentElement := txt:get-content-element($elem)
    
    let $preTextNodes := txt:outerNodes($elem, true(), $contentElement)
    let $postTextNodes := txt:outerNodes($elem, false(), $contentElement)
    
    let $preText := txt:toText($preTextNodes)
    let $postText := txt:toText($postTextNodes)
    return
        element context {
            element preText { txt:cutToWord(false(), $preText) },
            element mainText { txt:toText($elem) },
            element postText { txt:cutToWord(true(), $postText) }
        }
};

declare function txt:countWords($teiDoc as element(tei:TEI)) as xs:integer {
    sum(
        for $contentNode in $teiDoc//tei:text//*[local-name() = $txt:contentElements]
        return
            count(tokenize(txt:toText($contentNode), " "))
    )
};




xquery version "3.1";

(:~
 : A set of helper functions to transform TEI data to other formats (only text for now)
 :)
module namespace txt="http://histei.info/xquery/tei2text";

import module namespace functx="http://www.functx.com" at "functx.xql";
import module namespace teix="http://histei.info/xquery/tei" at "tei.xqm";
import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace tei = "http://www.tei-c.org/ns/1.0";

declare %private variable $txt:CONTEXT_LENGTH := 255;

declare %private variable $txt:REMOVED_ELEMENTS := ("del", "note", "fw");
declare %private variable $txt:REPLACED_ELEMENTS := map{ "gap" : "(GAP)" };
declare %private variable $txt:WRAPPED_ELEMENTS := map{
    "abbr" : ("_", "_"),
    "expan" : ("[", "]"),
    "supplied" : ("{", "}"),
    "unclear" : ("!", "!")
};

(: Functions for TEI fields :)

declare %private variable $txt:CERTAINTY := map { 
    "unknown" : "",
    "low" : "*",
    "medium" : "",
    "high" : "^"
};

declare %private variable $txt:CREATION_TYPES := 
    ("author", "deponent", "deposed", "deposition", "sender", "sent", "written");

declare function txt:id-as-label($id as xs:string?) as xs:string? {
   replace($id, "_", " ") 
};

declare function txt:category($category as element(tei:category)?) as xs:string* {
    (
       txt:id-as-label($category/@xml:id), 
       normalize-space($category/tei:catDesc/text())
    )
};

declare function txt:place($place as element(tei:place)?) as xs:string? {
    normalize-space($place/tei:placeName[1]/text())
};

declare function txt:org($org as element(tei:org)?) as xs:string? {
    normalize-space($org/tei:orgName[1]/text())
};

declare function txt:person($person as element(tei:person)?) as xs:string? {
    if (empty($person)) then
        ()
    else
        let $name := txt:name-info($person/tei:persName[1])
        let $birth := txt:year-info($person/tei:birth[1])
        let $death := txt:year-info($person/tei:death[1])
        let $dates := 
            if ($birth("year") or $death("year")) then 
                concat("(", $birth("year"), "-", $death("year"), ")")
            else 
                ""
        return
            normalize-space(string-join(($name("forename"), $name("surname"), $dates), " "))
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
            map:merge((
                if ($role) then map:entry("role", $role) else (),
                if ($forename) then map:entry("forename", $forename) else (),
                if ($maiden) then map:entry("maiden", $maiden) else (),
                if ($surname) then map:entry("surname", $surname) else ()
            ))
    else
        ()
};

declare function txt:year-info($datable as element()?, $single-estimates as xs:boolean) as map(xs:string, item()) {
    if (empty($datable)) then
        map{}
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
                map { "year" : $when, "cert" : $cert }
            
            case exists($notBefore) and exists($notAfter) return
                if ($single-estimates) then 
                    map { "year" : (($notAfter - $notBefore) idiv 2) + $notBefore, "cert" : "nb/na" }
                else
                    map { "year" : concat($notBefore, "-", $notAfter), "cert" : "nb-na" }
            
            case exists($notBefore) return 
                map { "year" : $notBefore, "cert" : "nb" }
            
            case exists($notAfter) return 
                map { "year" : $notAfter, "cert" : "na" }
            
            case exists($from) or exists($to) return
                map { "year" : if ($from eq $to) then $from else concat($from, "-", $to), "cert" : $cert }
                
            default return 
                map{}
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

declare function txt:is-genre($element as element(tei:catRef)) as xs:boolean {
    let $scheme := string($element/@scheme)
    return
        $scheme eq "" or contains($scheme, "EMST_GENRES")
};

(:~
 : Returns the single catRef (if it exists) that represents the main text genre
 : 
 : @param $tei A single TEI element
 : @return A single catRef element (if it exists), containing a reference to the main text genre
:)
declare function txt:genre($tei as element(tei:TEI)) as element(tei:catRef)? {
    head(
        for $catRef in $tei/tei:teiHeader/tei:profileDesc/tei:textClass/tei:catRef
        return
            if (txt:is-genre($catRef)) then
                $catRef
            else
                ()
    )
};

declare function txt:is-place($element as element()?) as xs:boolean {
    local-name($element) = $teix:PLACE_ELEMENT_NAMES
};

(:~
 : Returns one element for each entity located beneath the TEI creation element (i.e. date, org, person, place)
 :  that represents the entity that was key in creating the document (e.g. for a letter the person where @type equals "sender")
 : 
 : @param $tei A single TEI element
 : @return A set of maximum four elements, one for each entity below the TEI creation element, if present (i.e. date, org, person, place)
:)
declare function txt:creation($tei as element(tei:TEI)) {
    let $creation := $tei/tei:teiHeader/tei:profileDesc/tei:creation[1]
    return
        if (empty($creation)) then
            ()
        else
            let $filterFunc := function($elements as element()*, $creationType as xs:string) as element()? {
                let $createdElements := $elements[@type = $txt:CREATION_TYPES]
                return
                    if (empty($createdElements)) then $elements[empty(@type)][1] else $createdElements[1]
            }
            return (
                $filterFunc($creation/tei:date, "date"),
                $filterFunc($creation/tei:orgName, "org"),
                $filterFunc($creation/tei:persName, "person"),
                $filterFunc($creation/*[txt:is-place(.)], "place")
            )
};

(:~
 : Returns the current status of the document as the latest TEI change element or
 :  if status is set on the revisionDesc element, the latest TEI change element with the same status
 :  otherwise, it returns the string value of the status attribute on the revisionDesc element
 : 
 : @param $tei One or more TEI elements
 : @return Either the most recent TEI change element or a string with the document's global status, on revisionDesc
:)
declare function txt:status($tei as element(tei:TEI)*) {
    for $doc in $tei
    let $revisionDesc := $doc//tei:revisionDesc[1]
    let $docStatus := $revisionDesc/@status
    let $changes := for $change in $revisionDesc/tei:change order by $change/@when return $change
    return
        if (exists($docStatus) and $docStatus ne "") then
            let $change := $changes[@status eq $docStatus][last()]
            return
                if (empty($change)) then
                    $docStatus
                else
                    $change
        else
           $changes[last()]
};

(:~
 : Returns all valid text() nodes within a TEI annotation element (e.g. num, pc, w)
 : - Text within del elements is ignored
 : - punctuation directly preceding a line or page break is ignored (e.g. tegen<pc>=</pc><lb/>woordigh becomes tegenwoordigh)
 : 
 : @param $ann The TEI annotation element (e.g. num, pc, w)
 : @return The set of all valid text() nodes within the annotation element
:)
declare %private function txt:attested-form($ann as element()?) as text()* {
    for $node in $ann/node()
    return
        typeswitch($node)
        case text() return 
            $node
        case element() return
            switch(local-name($node))
            case "del" return
                ()
            case "pc" return
                let $followingNode := $node/following::node()[1]
                return
                    if ($followingNode instance of element() 
                        and local-name($followingNode) = ("lb", "pb")) then
                        ()
                    else
                        txt:attested-form($node)
            default return
                txt:attested-form($node)
        default return
            ()
};

(:~
 : Returns a string representation of the attested form from TEI num, pc or w elements
 : - Text within del elements is ignored
 : - punctuation directly preceding a line or page break is ignored (e.g. tegen<pc>=</pc><lb/>woordigh becomes tegenwoordigh)
 : 
 : @param $anns One or more TEI annotation elements (e.g. num, pc, w)
 : @return A set of attested forms, one for each TEI annotation element given
:)
declare function txt:attested-forms($anns as element()*) as xs:string* {
    for $ann in $anns
    return
        normalize-space(string-join(txt:attested-form($ann), ""))
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
                if ($name = $teix:MILESTONE_ELEMENT_NAMES) then
                    if ($node/@break eq "no") then "" else " "
                
                else if ($name eq "seg" and $node/@function eq "formulaic") then 
                    let $wrappedNodes := txt:transformNodes($node/node())
                    return
                        if (exists($wrappedNodes)) then
                            string-join(("^", $wrappedNodes, "^"))
                        else
                            ""
                
                else if ($name = $txt:REMOVED_ELEMENTS) then ""
                
                else if (map:contains($txt:REPLACED_ELEMENTS, $name)) then $txt:REPLACED_ELEMENTS($name)
                
                else if (map:contains($txt:WRAPPED_ELEMENTS, $name)) then
                    let $wrappers := $txt:WRAPPED_ELEMENTS($name)
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

declare %private function txt:siblingNodes($nodes as node()*, $previous as xs:boolean, $currTextLength as xs:integer) as xs:string* {
    if (empty($nodes)) then 
        ()
    else
        let $currNode := if ($previous) then $nodes[last()] else $nodes[1] 
        let $text := replace(string-join(txt:transformNodes($currNode)), "\s+", " ")
        let $textLength := string-length($text)
        let $currTextLength := $currTextLength + $textLength
        return
            if ($currTextLength ge $txt:CONTEXT_LENGTH) then
                $text
            else if ($previous) then 
                (txt:siblingNodes(subsequence($nodes, 1, count($nodes) - 1), $previous, $currTextLength), $text) 
            else
                ($text, txt:siblingNodes(subsequence($nodes, 2), $previous, $currTextLength)) 
};

declare %private function txt:outerNodes($element as element()?, $previous as xs:boolean, 
                                    $topElement as element()?, $currTextLength as xs:integer?) as xs:string* {
    
    if (empty($element) or (exists($topElement) and ($topElement is $element))) then
        ()
    else
        let $currTextLength := if (empty($currTextLength)) then 0 else $currTextLength 
        let $parent := $element/parent::*
        let $nextNodes := utils:sibling($element, $previous)
        (: Originally tried this with preceding-sibling, but that does NOT scale well, but this way is faster by a factor of 10 or so  :)
        (:let $nextNodes := if ($previous) then $element/preceding-sibling::node() else $element/following-sibling::node():)
        let $text := replace(string-join(txt:siblingNodes($nextNodes, $previous, $currTextLength)), "\s+", " ")
        let $textLength := string-length($text)        
        return
            if ($currTextLength ge $txt:CONTEXT_LENGTH) then
                $text
            else if ($previous) then 
                (txt:outerNodes($parent, $previous, $topElement, $currTextLength), $text) 
            else
                ($text, txt:outerNodes($parent, $previous, $topElement, $currTextLength))
};

declare %private function txt:outerNodes($element as element()?, $previous as xs:boolean, $topElement as element()?) as xs:string* {
    txt:outerNodes($element, $previous, $topElement, ())
};

declare function txt:cutToWord($start as xs:boolean, $input as xs:string?, $outputLength as xs:integer?) as xs:string? {
    let $continuedSymbol := "&#8230;"
    let $outputLength := if (empty($outputLength) or $outputLength < 1) then $txt:CONTEXT_LENGTH else $outputLength
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

declare function txt:context($element as element()) as element(context) {
    let $contentElement := teix:content-element($element)
    
    let $preText := normalize-space(string-join(txt:outerNodes($element, true(), $contentElement)))
    let $postText := normalize-space(string-join(txt:outerNodes($element, false(), $contentElement)))
    return
        element context {
            element preText { txt:cutToWord(false(), $preText) },
            element mainText { txt:toText($element) },
            element postText { txt:cutToWord(true(), $postText) }
        }
};

declare function txt:countWords($teiDoc as element(tei:TEI)) as xs:integer {
    sum(
        for $contentNode in $teiDoc//tei:text//*[local-name() = $teix:CONTENT_ELEMENT_NAMES]
        return
            count(tokenize(txt:toText($contentNode), " "))
    )
};




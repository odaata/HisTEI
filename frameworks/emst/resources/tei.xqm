xquery version "3.0";

(:~
 : A set of helper functions to handle TEI data
 :)
module namespace teix="http://cohd.info/xquery/tei";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";

declare namespace tei="http://www.tei-c.org/ns/1.0";

declare variable $teix:NS := "http://www.tei-c.org/ns/1.0";
declare variable $teix:CERTAINTY := map { 
    "unknown" := "",
    "low" := "*",
    "medium" := "",
    "high" := "^"
};

declare function teix:category($category as element(tei:category)) as xs:string+ {
    (replace($category/@xml:id, "_", " "), normalize-space($category/tei:catDesc/text()))
};

declare function teix:place($place as element(tei:place)) as xs:string {
    $place/tei:placeName/text()
};

declare function teix:org($org as element(tei:org)) as xs:string {
    $org/tei:orgName/text()
};

declare function teix:person($person as element(tei:person)) as xs:string {
    let $name := teix:name-info($person/tei:persName)
    let $birth := teix:year-info($person/tei:birth)
    let $death := teix:year-info($person/tei:death)
    let $dates := 
        if ($birth("year") or $death("year")) then 
            concat("(", $birth("year"), "-", $death("year"), ")")
        else 
            ""
    return
        string-join(($name("forename"), $name("surname"), $dates), " ")
};

declare function teix:name-info($persName as element(tei:persName)?) as map(xs:string, xs:string)? {
    
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

declare function teix:format-year($datable as element(), $single-estimates as xs:boolean) as xs:string {
    let $when := teix:year($datable/@when)
    let $notBefore := teix:year($datable/@notBefore)
    let $notAfter := teix:year($datable/@notAfter)
    let $from := teix:year($datable/@from)
    let $to := teix:year($datable/@to)
    let $cert := if ($datable/@cert) then $teix:CERTAINTY($datable/@cert) else ""
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

declare function teix:format-year($datable as element()) as xs:string {
    teix:format-year($datable, true())
};

declare function teix:year-info($datable as element(), $single-estimates as xs:boolean) as map(xs:string, item()) {
    let $when := teix:year($datable/@when)
    let $notBefore := teix:year($datable/@notBefore)
    let $notAfter := teix:year($datable/@notAfter)
    let $from := teix:year($datable/@from)
    let $to := teix:year($datable/@to)
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

declare function teix:year-info($datable as element()) as map(xs:string, item())? {
    teix:year-info($datable, true())
};

declare function teix:year($date as xs:string?) as xs:integer? {
    if (matches($date, "^\d{4}")) then
        xs:integer(substring($date, 1, 4))
    else
        ()
};

declare function teix:node($node-name as xs:string, $content as item()*) as element() {
    element { fn:QName($teix:NS, $node-name) } { $content }
};
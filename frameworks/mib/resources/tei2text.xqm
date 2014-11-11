xquery version "3.0";

(:~
 : A set of helper functions to transform TEI data to other formats (only text for now)
 :)
module namespace txt="http://cohd.info/xquery/tei2text";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace tei = "http://www.tei-c.org/ns/1.0";

import module namespace functx="http://www.functx.com" at "functx.xql";

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




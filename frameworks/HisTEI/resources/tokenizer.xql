xquery version "3.0";

import module namespace functx="http://www.functx.com" at "functx.xql";

declare default element namespace "http://www.tei-c.org/ns/1.0";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace uuid = "java:java.util.UUID";

declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";
declare option output:omit-xml-declaration "no";
declare option output:indent "no";

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

declare function local:contains-ws-element($element as element()?) as xs:boolean {
    exists($element//text()[local:contains-ws(.) and not(local:is-empty-oxy-comment(.))])
};

declare function local:is-element-break($element as element()?) as xs:boolean {
    let $elementName := local-name($element)
    return
        if ($elementName = $breakNames) then
            ($element/@break ne "no")
        else if ($elementName eq "pc") then
            ($element/@force ne "weak")
        else
            false()
};

declare function local:is-empty-oxy-comment($textNode as node()?) as xs:boolean {
    (
        exists($textNode) 
        and $textNode instance of text()
        and $textNode/preceding-sibling::node()[1] instance of processing-instruction(oxy_comment_start)
        and $textNode/following-sibling::node()[1] instance of processing-instruction(oxy_comment_end)
        and normalize-space($textNode) eq ""
    )
};

declare function local:non-empty-text-nodes($element as element()) as text()* {
    $element//text()[normalize-space() ne ""]
};

declare function local:trimTextNode($textNode as text()?) as text()? {
    if (exists($textNode)) then
        if (local:is-empty-oxy-comment($textNode)) then
            $textNode
        else
            let $preceding := $textNode/preceding::node()[self::* or (self::text() and not(local:is-empty-oxy-comment(.)))][1]
            let $isBegin := (local-name($preceding) = $breakNames)
                    
            let $following := $textNode/following::node()[self::* or (self::text() and not(local:is-empty-oxy-comment(.)))][1]
            let $isEnd := (local-name($following) = $breakNames)
            
            let $text := 
                if ($isBegin and $isEnd) then
                    normalize-space($textNode)
                else
                    let $str := replace($textNode, "\s+", " ")
                    return
                        if ($isBegin) then
                            functx:left-trim($str)
                        else if ($isEnd) then
                            functx:right-trim($str)
                        else
                            $str
            return
                if ($text eq "") then () else text { $text }
    else
        ()
};

declare function local:clean-spaces($element as element()) {
    let $trimmedElement := local:replace-content($element,
        for $node in $element/node()
        return
            typeswitch ($node)
            case text() return
                local:trimTextNode($node)
            case element() return
                local:clean-spaces($node)
            default return
                $node
    ) 

    let $firstChild := $trimmedElement/(text()[not(local:is-empty-oxy-comment(.))] | *)[1]
    let $lastChild := $trimmedElement/(text()[not(local:is-empty-oxy-comment(.))] | *)[last()]
    
    return
        if (empty($firstChild)) then
            $trimmedElement
        else
            let $addBeginSpace := 
                if ($firstChild instance of text()) then 
                    local:contains-ws(local:trimTextNode($firstChild), "starts") 
                else 
                    false()
            
            let $addEndSpace := 
                if ($lastChild instance of text()) then 
                    local:contains-ws(local:trimTextNode($lastChild), "ends") 
                else 
                    false()
            
            let $childNodes :=
                for $node in $trimmedElement/node()
                return
                    typeswitch ($node)
                    case text() return
                        let $trimmedText := local:trimTextNode($node)
                        return
                            if ($firstChild is $lastChild) then
                                if ($node is $firstChild) then 
                                    text { normalize-space($trimmedText) }
                                else
                                    $trimmedText
                            else
                                if ($addBeginSpace and $node is $firstChild) then
                                    text { functx:left-trim($trimmedText) }
                                else if ($addEndSpace and $node is $lastChild) then
                                    text { functx:right-trim($trimmedText) }
                                else
                                    $trimmedText
                    default return
                        $node
            
            let $textSpace := text { " " }
            return
            (
                if ($addBeginSpace) then $textSpace else (),
                local:replace-content($trimmedElement, $childNodes),
                if ($addEndSpace) then $textSpace else ()
            )
};

declare function local:clean-spaces-doc($element as element()) as element() {
    local:replace-content($element, 
        for $node in $element/node()
        return
            typeswitch($node)
            case element() return
                if (local-name($node) = $contentNames) then
                    local:clean-spaces($node)
                else
                    local:clean-spaces-doc($node)
            default return
                $node
    )
};

declare function local:split-sub-word-elements-doc($element as element()) as element() {
    local:replace-content($element, 
        for $node in $element/node()
        return
            typeswitch($node)
            case element() return
                let $elementName := local-name($node)
                return
                    if ($elementName = $subWordToSplitNames) then
                        let $tokenFunc := function($content) { local:replace-content($node, $content) }
                        return
                            local:tokenize($tokenFunc, $node/node())
                    else
                        local:split-sub-word-elements-doc($node)
            default return
                $node
    )
};

declare function local:token($tokenFunc as function(item()*) as element()*, $content) as node()* {
    if (empty($content)) then
        $content
    else if (count($content) gt 1) then
        $tokenFunc($content)
    else
        typeswitch($content)
        case element() return
            let $elementName := local-name($content)
            return
                if ($elementName = ($milestoneNames, "gap")) then
                    $content
                else if (exists($elementName) and not($elementName = $subWordNames)) then
                    local:replace-content($content, local:tokenize($tokenFunc, $content/node()))
                else
                    $tokenFunc($content)
        
        case comment() return $content
        case processing-instruction() return $content
        default return
            $tokenFunc($content)
};

declare function local:tokenize-text($text as text(), $tokenFunc as function(item()*) as element()*, 
                                        $nodes as node()*, $nextN as xs:integer?, $currentToken) {
    if (local:contains-ws($text, "all")) then
        ( local:token($tokenFunc, $currentToken), " ", local:tokenize($tokenFunc, $nodes, $nextN) )
    else
        let $tokens := tokenize($text, "\s+")
        let $numTokens := count($tokens)
        return
            if ($numTokens eq 1) then
                local:tokenize($tokenFunc, $nodes, $nextN, ($currentToken, $tokens))
            else
                let $tokenized := 
                    for $token at $i in $tokens
                    return
                        if ($i eq 1) then
                            if ($token eq "") then
                                ( local:token($tokenFunc, $currentToken), " " )
                            else
                                ( local:token($tokenFunc, ($currentToken, $token)), " " )
                        else if ($i eq $numTokens) then
                            ()
                        else
                            ( local:token($tokenFunc, $token), " " )
                
                let $lastToken 
                := $tokens[last()]
                return
                ( 
                    $tokenized, if ($lastToken eq "") then " " else (),
                    local:tokenize($tokenFunc, $nodes, $nextN, if ($lastToken eq "") then () else $lastToken)
                )
};

declare function local:tokenize-element($element as element(), $tokenFunc as function(item()*) as element()*,
                                            $nodes as node()*, $nextN as xs:integer?, $currentToken) {
    let $elementName := local-name($element)
    return
        if ($elementName = $subWordNeverTokenizedNames) then
            ( local:token($tokenFunc, $currentToken), local:token($tokenFunc, $element), local:tokenize($tokenFunc, $nodes, $nextN) )
        else if ($elementName = ($breakNames, $annotationNames)) then
            if ($element/@break eq "no" or $element/@force eq "weak") then
                local:tokenize($tokenFunc, $nodes, $nextN, ($currentToken, $element))
            else
                ( local:token($tokenFunc, $currentToken), $element, local:tokenize($tokenFunc, $nodes, $nextN) )
        else if (local:contains-ws-element($element) 
                or not($elementName = ($subWordNames, $editNames, $milestoneNames))) then
        ( 
            local:token($tokenFunc, $currentToken), 
            local:replace-content($element, local:tokenize($tokenFunc, $element/node())), 
            local:tokenize($tokenFunc, $nodes, $nextN) 
        )
        else
            local:tokenize($tokenFunc, $nodes, $nextN, ($currentToken, $element))
};                                        

declare function local:tokenize($tokenFunc as function(item()*) as element()*, 
                                    $nodes as node()*, $n as xs:integer?, $currentToken) {
    if (empty($nodes)) then
        $nodes
    else
        let $n := if (empty($n) or $n < 1) then 1 else $n
        let $numNodes := count($nodes)
        let $nextN := $n + 1
        return
            if ($n <= $numNodes) then
                let $node := $nodes[$n]
                return
                    typeswitch($node)
                    case text() return
                        local:tokenize-text($node, $tokenFunc, $nodes, $nextN, $currentToken)
                    case element() return
                        local:tokenize-element($node, $tokenFunc, $nodes, $nextN, $currentToken)
                    default return
                        local:tokenize($tokenFunc, $nodes, $nextN, ($currentToken, $node))
            else
                local:token($tokenFunc, $currentToken)
};

declare function local:tokenize($tokenFunc as function(item()*) as element()*, 
                                    $nodes as node()*, $n as xs:integer?) {
    local:tokenize($tokenFunc, $nodes, $n, ())
};

declare function local:tokenize($tokenFunc as function(item()*) as element()*, $nodes as node()*) {
    local:tokenize($tokenFunc, $nodes, ())
};

declare function local:annotate-token($content) as element()* {
    element w {
        (:attribute xml:id { uuid:randomUUID() },:)
        $content
    }
};

declare function local:tokenize-punct($word as element(w)) {
    let $firstChild := $word//text()[not(local:is-empty-oxy-comment(.))][1]
    let $lastChild := $word//text()[not(local:is-empty-oxy-comment(.))][last()]
    return
        if (empty($firstChild)) then
            $word
        else
            element dude {}
            (:let $addBeginPunct := matches($firstChild, "^\p{P}+")
            let $addEndPunct := matches($lastChild, "\p{P}+$")
            
            let $childNodes :=
                for $node in $word/node()
                return
                    typeswitch ($node)
                    case text() return
                            if ($firstChild is $lastChild) then
                                if ($node is $firstChild) then 
                                    text { normalize-space($trimmedText) }
                                else
                                    $trimmedText
                            else
                                if ($addBeginPunct and $node is $firstChild) then
                                    text { functx:left-trim($trimmedText) }
                                else if ($addEndPunct and $node is $lastChild) then
                                    text { functx:right-trim($trimmedText) }
                                else
                                    $trimmedText
                    default return
                        $node
            
            let $textSpace := text { " " }
            return
            (
                if ($addBeginPunct) then $textSpace else (),
                local:replace-content($trimmedElement, $childNodes),
                if ($addEndPunct) then $textSpace else ()
            ):)
};

declare function local:tokenize-doc($element as element()) as element() {
    local:replace-content($element, 
        for $node in $element/node()
        return
            typeswitch($node)
            case element() return
                if (exists($node/ancestor::body) and local-name($node) = $contentNames) then
                    local:replace-content($node, local:tokenize(local:annotate-token#1, $node/node()))
                else
                    local:tokenize-doc($node)
            default return
                $node
    )
};


let $trans := doc("file:///home/mike/Amsterdam/transcriptions/SAA_05061_Schout-Sch_00567_0000000667.xml")
(:let $trans := doc("file:///home/mike/Amsterdam/test-letter.xml"):)
let $trimmedTrans := local:clean-spaces-doc($trans/TEI)
let $splitTrans := local:split-sub-word-elements-doc($trimmedTrans)
let $p := ($trimmedTrans//p)[last()]
let $p := $trimmedTrans/text[1]/body[1]/div[66]/p[1]
return
(:    $splitTrans:)
    local:tokenize-doc($splitTrans)
    
(:    element results {
        let $endPunct := "/!du/de–er—in=o!!"
        let $beginPunct := "?Que"
        let $midPunct := "duder-ino"
        let $tokens := tokenize($endPunct, "\W")
        return
            analyze-string($endPunct, "(\p{Po}+)|(\p{P}+|=+)")
    }:)





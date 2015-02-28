xquery version "3.0";

(:~
 : A tokenizer for splitting up words, numbers and punctuation within TEI documents
 :
 : @author Mike Olson
 : @version 0.1
 :)
 
module namespace tok="http://histei.info/xquery/tei/tokenizer";

import module namespace functx="http://www.functx.com" at "functx.xql";
import module namespace teix="http://histei.info/xquery/tei" at "tei.xqm";
import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare default element namespace "http://www.tei-c.org/ns/1.0";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";

(: Tokenization Regex Character Classes - can also be passed in externally :)
declare variable $tok:numberBreakClass as xs:string external := ":\.\-/'""%";
declare variable $tok:numberClass as xs:string external := "\p{N}";
declare variable $tok:nonBreakingPunctClass as xs:string external := "\p{Pd}\p{Pc}'=";
declare variable $tok:breakingPunctClass as xs:string external := "\p{S}\p{P}";
declare variable $tok:whitespaceClass as xs:string external := "\s";
declare variable $tok:wordClass as xs:string external := "\p{L}\p{M}";

(: TEI-specific element names and their use in our corpus:)
declare %private variable $tok:contentNames := ("p", "head", "dateline", "signed", "salute", 
                                    "byline", "argument", "epigraph", "trailer", "address");
declare %private variable $tok:breakNames := ( "cb", "gb", "lb", "milestone", "pb");
declare %private variable $tok:milestoneNames := ($tok:breakNames, "handShift");
declare %private variable $tok:subWordToSplitNames := ("expan", "supplied", "unclear");
declare %private variable $tok:subWordNeverTokenizedNames := ("abbr");
declare %private variable $tok:subWordNames := ($tok:subWordToSplitNames, $tok:subWordNeverTokenizedNames, "gap");
declare %private variable $tok:annotationNames := ("w", "pc", "num");
declare %private variable $tok:editNames := ("add", "del", "hi");

declare %private variable $tok:TYPE_LABEL := "type";
declare %private variable $tok:REGEX_LABEL := "regex";
declare %private variable $tok:IS_BREAK_LABEL := "isBreak";
declare %private variable $tok:ANN_FUNC_LABEL := "annFunc";

(: Default Token Types to be used if none passed in - this can be used as a model outside the module to pass in custom token types :)
declare variable $tok:defaultTokenTypes as map(xs:integer, map(xs:string, item())) := tok:token-types();

 declare %private function tok:token-types() as map(xs:integer, map(xs:string, item())) {
    let $numberClassRegex := concat("[", $tok:numberClass, "]")
    let $numberBreakClassRegex := concat("[", $tok:numberBreakClass, "]")
    let $wordClassRegex := concat("[", $tok:wordClass, "]")
    let $nonBreakingPunctClassRegex := concat("[", $tok:nonBreakingPunctClass, "]")
    
    let $ordinalNumberRegex := concat($numberClassRegex, "+", $wordClassRegex, "+")
    
    let $numberRegex := concat($numberClassRegex, "+[", $tok:numberBreakClass, $tok:numberClass, "]*|", 
        "[", $tok:numberClass, $tok:numberBreakClass, "]*", $numberClassRegex, "+", $numberBreakClassRegex, "*")
    
    let $compoundRegex := concat($wordClassRegex, "+", $nonBreakingPunctClassRegex, "+[", $tok:wordClass, $tok:nonBreakingPunctClass, "]*|", 
        "[", $tok:wordClass, $tok:nonBreakingPunctClass, "]*", $nonBreakingPunctClassRegex, "+", $wordClassRegex, "+")
        
    let $breakingPunctRegex := concat("\.\.\.|[", $tok:breakingPunctClass, "]")
    
    let $whitespaceRegex := concat("[", $tok:whitespaceClass, "]+")
    
    let $wordRegex := concat($wordClassRegex, "+")
    return
        map {
            1 := map { $tok:TYPE_LABEL := "ordinal", $tok:REGEX_LABEL := $ordinalNumberRegex, $tok:ANN_FUNC_LABEL := teix:num(?, "ordinal") },
            2 := map { $tok:TYPE_LABEL := "number", $tok:REGEX_LABEL := $numberRegex, $tok:ANN_FUNC_LABEL := tok:num#1 },
            3 := map { $tok:TYPE_LABEL := "compound", $tok:REGEX_LABEL := $compoundRegex, $tok:ANN_FUNC_LABEL := teix:word#1 },
            4 := map { $tok:TYPE_LABEL := "breakingPunct", $tok:REGEX_LABEL := $breakingPunctRegex, $tok:IS_BREAK_LABEL := true(), $tok:ANN_FUNC_LABEL := teix:pc#1 },
            5 := map { $tok:TYPE_LABEL := "whitespace", $tok:REGEX_LABEL := $whitespaceRegex, $tok:IS_BREAK_LABEL := true() },
            6 := map { $tok:TYPE_LABEL := "word", $tok:REGEX_LABEL := $wordRegex, $tok:ANN_FUNC_LABEL := teix:word#1 },
        }
};

 declare %private function tok:token-type-name($tokenTypes as map(xs:integer, map(xs:string, item())), 
                                            $tokenKey as xs:integer?) as xs:string? {
    let $tokenType := $tokenTypes($tokenKey)
    return
        if (exists($tokenType)) then 
            $tokenType($tok:TYPE_LABEL) 
        else 
            ()
};

 declare %private function tok:regex($tokenTypes as map(xs:integer, map(xs:string, item())), 
                                $tokenKey as xs:integer?) as xs:string? {
    let $tokenType := $tokenTypes($tokenKey)
    return
        if (exists($tokenType)) then 
            $tokenType($tok:REGEX_LABEL) 
        else 
            ()
};

 declare %private function tok:ann-func($tokenTypes as map(xs:integer, map(xs:string, item())), 
                                    $tokenKey as xs:integer?) as (function(item()*) as element()*)? {
    let $tokenType := $tokenTypes($tokenKey)
    return
        if (exists($tokenType)) then 
            $tokenType($tok:ANN_FUNC_LABEL) 
        else 
            ()
};

 declare %private function tok:run-ann-func($tokenTypes as map(xs:integer, map(xs:string, item())), 
                                    $tokenKey as xs:integer?, $content) {
    let $annFunc := tok:ann-func($tokenTypes, $tokenKey)
    return
        if (exists($annFunc)) then 
            $annFunc($content) 
        else 
            $content
};

 declare %private function tok:is-break($tokenTypes as map(xs:integer, map(xs:string, item())), 
                                    $tokenKey as xs:integer?) as xs:boolean {
    let $tokenType := $tokenTypes($tokenKey)
    return
        if (exists($tokenType) and exists($tokenType($tok:IS_BREAK_LABEL))) then 
            $tokenType($tok:IS_BREAK_LABEL) 
        else 
            false()
};

 declare %private function tok:num($content) as element(num)? {
    if (empty($content)) then
        ()
    else
        let $numberClassRegex := concat("[", $tok:numberClass, "]")
        let $numTypes := map { 
            "ordinal" := $tok:defaultTokenTypes(1)($tok:REGEX_LABEL), 
            "fraction" := concat($numberClassRegex, "+/", $numberClassRegex, "+"), 
            "percentage" := concat($numberClassRegex, "+%"),
            "decimal" := concat($numberClassRegex, "+([,:\.]", $numberClassRegex, "+)+"),
            "cardinal" := concat("^", $numberClassRegex, "+[\.]*$")
        }
        let $num := element num { $content }
        let $text := string($num)
        let $type := 
            for $key in map:keys($numTypes)
            return
                if (matches($text, $numTypes($key))) then
                    $key
                else
                    ()
        return
            teix:num($content, $type)
};

 declare %private function tok:trimTextNode($textNode as text()?) as text()? {
    if (exists($textNode)) then
        if (utils:is-empty-oxy-comment($textNode)) then
            $textNode
        else
            let $preceding := $textNode/preceding::node()[self::* or (self::text() and not(utils:is-empty-oxy-comment(.)))][1]
            let $isBegin := (local-name($preceding) = $tok:breakNames)
                    
            let $following := $textNode/following::node()[self::* or (self::text() and not(utils:is-empty-oxy-comment(.)))][1]
            let $isEnd := (local-name($following) = $tok:breakNames)
            
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

 declare %private function tok:clean-spaces($element as element()) {
    let $trimmedElement := utils:replace-content($element,
        for $node in $element/node()
        return
            typeswitch ($node)
            case text() return
                tok:trimTextNode($node)
            case element() return
                tok:clean-spaces($node)
            default return
                $node
    ) 

    let $firstChild := $trimmedElement/(text()[not(utils:is-empty-oxy-comment(.))] | *)[1]
    let $lastChild := $trimmedElement/(text()[not(utils:is-empty-oxy-comment(.))] | *)[last()]
    
    return
        if (empty($firstChild)) then
            $trimmedElement
        else
            let $addBeginSpace := 
                if ($firstChild instance of text()) then 
                    utils:contains-ws(tok:trimTextNode($firstChild), "starts") 
                else 
                    false()
            
            let $addEndSpace := 
                if ($lastChild instance of text()) then 
                    utils:contains-ws(tok:trimTextNode($lastChild), "ends") 
                else 
                    false()
            
            let $childNodes :=
                for $node in $trimmedElement/node()
                return
                    typeswitch ($node)
                    case text() return
                        let $trimmedText := tok:trimTextNode($node)
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
                utils:replace-content($trimmedElement, $childNodes),
                if ($addEndSpace) then $textSpace else ()
            )
};

 declare %private function tok:clean-spaces-doc($element as element()) as element() {
    utils:replace-content($element, 
        for $node in $element/node()
        return
            typeswitch($node)
            case element() return
                if (local-name($node) = $tok:contentNames) then
                    tok:clean-spaces($node)
                else
                    tok:clean-spaces-doc($node)
            default return
                $node
    )
};

 declare %private function tok:sub-word-elements-token-types($annFunc as function(item()*) as element()*) 
                                                        as map(xs:integer, map(xs:string, item())) {
    map:new(
        for $key in map:keys($tok:defaultTokenTypes)
        let $typeName := tok:token-type-name($tok:defaultTokenTypes, $key)
        let $tokenMap := $tok:defaultTokenTypes($key)
        return
            map:entry(
                $key, 
                if ($typeName = ("ordinal", "number", "compound", "word")) then 
                    map:new(($tokenMap, map { $tok:ANN_FUNC_LABEL := $annFunc }))
                else
                    map:remove($tokenMap, $tok:ANN_FUNC_LABEL)
            )
    )
};

 declare %private function tok:split-sub-word-elements-doc($element as element()) as element() {
    utils:replace-content($element, 
        for $node in $element/node()
        return
            typeswitch($node)
            case element() return
                let $elementName := local-name($node)
                return
                    if ($elementName = $tok:subWordToSplitNames) then
                        let $annFunc := function($content) { utils:replace-content($node, $content) }
                        let $tokenTypes := tok:sub-word-elements-token-types($annFunc)
                        return
                            tok:tokenize-nodes($tokenTypes, $node/node())
                    else
                        tok:split-sub-word-elements-doc($node)
            default return
                $node
    )
};

 declare %private function tok:token($tokenTypes as map(xs:integer, map(xs:string, item())), $content) {
    if (empty($content)) then
        $content
    else 
        let $key := 
            let $token := element w { $content }
            let $token := xs:string($token)
            let $tokenSplit := tok:split-text($tokenTypes, $token)
            return
                if (count($tokenSplit) eq 1) then
                    map:keys($tokenSplit)[1]
                else
                    (: If there are multiple tokens, then just use default annFunc for 'word' token type :)
                    6
        return
            if (count($content) gt 1) then
                tok:run-ann-func($tokenTypes, $key, $content)
            else
                typeswitch($content)
                case element() return
                    let $elementName := local-name($content)
                    return
                        if ($elementName = ($tok:milestoneNames, "gap")) then
                            $content
                        else if (exists($elementName) and not($elementName = $tok:subWordNames)) then
                            utils:replace-content($content, tok:tokenize-nodes($tokenTypes, $content/node()))
                        else
                            tok:run-ann-func($tokenTypes, $key, $content)
                
                case comment() return $content
                case processing-instruction() return $content
                default return
                    tok:run-ann-func($tokenTypes, $key, $content)
};

 declare %private function tok:compound($compound as xs:string?) {
    if (empty($compound)) then
        $compound
    else
        let $split := analyze-string($compound, concat("[", $tok:nonBreakingPunctClass, "]"))
        for $part in $split/*
        return
            typeswitch($part)
            case element(fn:match) return
                teix:pc($part/text(), "weak")
            case element(fn:non-match) return
                $part/text()
            default return
                ()
};

 declare %private function tok:split-text($tokenTypes as map(xs:integer, map(xs:string, item())), 
                                        $text as xs:string) as map(xs:integer, item()*)* {
    let $regexes := 
        for $key in map:keys($tokenTypes)
        let $tokenType := $tokenTypes($key)
        order by $key
        return
            concat("(", $tokenType($tok:REGEX_LABEL), ")")
                
    let $regex := string-join($regexes, "|")
    let $tokens := analyze-string($text, $regex)
    return
    (
        for $token in $tokens/*
        return
            typeswitch($token)
            case element(fn:match) return
                let $key := xs:integer($token/fn:group/@nr)
                let $text := xs:string($token/fn:group/text())
                let $typeName := tok:token-type-name($tokenTypes, $key)
                let $text := if ("compound" eq $typeName) then tok:compound($text) else $text
                return
                    map{ $key := $text }
            case element(fn:non-match) return
                map{ 0 := xs:string($token/text()) }
            default return
                ()
    )
};

 declare %private function tok:tokenize-text($text as text(), $tokenTypes as map(xs:integer, map(xs:string, item())), 
                                        $nodes as node()*, $nextN as xs:integer?, $currentToken) {
    let $tokens := tok:split-text($tokenTypes, $text)
    let $numTokens := count($tokens)
    return
        let $tokenized := 
            for $tokenMap at $i in $tokens
            let $key := map:keys($tokenMap)[1]
            let $token := $tokenMap($key)
            let $isBreak := tok:is-break($tokenTypes, $key)
            return
                if ($i eq 1 and exists($currentToken)) then
                    if ($isBreak) then
                        ( tok:token($tokenTypes, $currentToken), tok:run-ann-func($tokenTypes, $key, $token) )
                    else
                        if ($numTokens gt 1) then
                            tok:token($tokenTypes, ($currentToken, $token))
                        else
                            ()
                else if ($i eq $numTokens and not($isBreak)) then
                    ()
                else
                    tok:run-ann-func($tokenTypes, $key, $token)
                
        let $lastToken := 
            let $lastTokenMap := $tokens[last()]
            let $key := map:keys($lastTokenMap)[1]
            let $isBreak := tok:is-break($tokenTypes, $key)
            return
                if ($isBreak) then 
                    () 
                else
                    let $token := $lastTokenMap($key)
                    return
                        if ($numTokens eq 1) then
                            ( $currentToken, $token )
                        else
                            $token
                    
        return
            ( $tokenized, tok:tokenize-nodes($tokenTypes, $nodes, $nextN, $lastToken) )
};

 declare %private function tok:tokenize-element($element as element(), $tokenTypes as map(xs:integer, map(xs:string, item())),
                                            $nodes as node()*, $nextN as xs:integer?, $currentToken) {
    let $elementName := local-name($element)
    return
        if ($elementName = $tok:subWordNeverTokenizedNames) then
            ( tok:token($tokenTypes, $currentToken), tok:token($tokenTypes, $element), tok:tokenize-nodes($tokenTypes, $nodes, $nextN) )
        else if ($elementName = ($tok:breakNames, $tok:annotationNames)) then
            if ($element/@break eq "no" or $element/@force eq "weak") then
                tok:tokenize-nodes($tokenTypes, $nodes, $nextN, ($currentToken, $element))
            else
                ( tok:token($tokenTypes, $currentToken), $element, tok:tokenize-nodes($tokenTypes, $nodes, $nextN) )
        else if (count(tok:split-text($tokenTypes, xs:string($element))) gt 1 
            or not($elementName = ($tok:subWordNames, $tok:editNames, $tok:milestoneNames))) then
        ( 
            tok:token($tokenTypes, $currentToken), 
            utils:replace-content($element, tok:tokenize-nodes($tokenTypes, $element/node())), 
            tok:tokenize-nodes($tokenTypes, $nodes, $nextN) 
        )
        else
            tok:tokenize-nodes($tokenTypes, $nodes, $nextN, ($currentToken, $element))
};                                        

 declare %private function tok:tokenize-nodes($tokenTypes as map(xs:integer, map(xs:string, item())), 
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
                        tok:tokenize-text($node, $tokenTypes, $nodes, $nextN, $currentToken)
                    case element() return
                        tok:tokenize-element($node, $tokenTypes, $nodes, $nextN, $currentToken)
                    default return
                        tok:tokenize-nodes($tokenTypes, $nodes, $nextN, ($currentToken, $node))
            else
                tok:token($tokenTypes, $currentToken)
};

 declare %private function tok:tokenize-nodes($tokenTypes as map(xs:integer, map(xs:string, item())), 
                                    $nodes as node()*, $n as xs:integer?) {
    tok:tokenize-nodes($tokenTypes, $nodes, $n, ())
};

 declare function tok:tokenize-nodes($tokenTypes as map(xs:integer, map(xs:string, item())), $nodes as node()*) {
    tok:tokenize-nodes($tokenTypes, $nodes, ())
};

 declare %private function tok:tokenize-doc($tokenTypes as map(xs:integer, map(xs:string, item())), $element as element()) as element() {
    utils:replace-content($element, 
        for $node in $element/node()
        return
            typeswitch($node)
            case element() return
                if (exists($node/ancestor::body) and local-name($node) = $tok:contentNames) then
                    utils:replace-content($node, tok:tokenize-nodes($tokenTypes, $node/node()))
                else
                    tok:tokenize-doc($tokenTypes, $node)
            default return
                $node
    )
};

 declare %private function tok:update-header($tei as element(TEI), $userID as xs:string?) as element(TEI) {
    let $wordCount := count($tei//w | $tei//num)
    
    let $teiHeader := $tei/teiHeader[1]
    let $fileDesc := $teiHeader/fileDesc[1]
    let $extent := teix:update-extent($wordCount, "words", $fileDesc/extent[1])
    
    let $fileDescContents := $fileDesc/node() except $fileDesc/extent
    let $pubStmtPos := index-of($fileDescContents, ($fileDesc/publicationStmt | $fileDesc/sourceDesc)[1])
    let $fileDescContents := if (exists($pubStmtPos)) then insert-before($fileDescContents, $pubStmtPos, $extent) else ($fileDescContents, $extent)
    let $newFileDesc := utils:replace-content($fileDesc, $fileDescContents )
    
    let $change := teix:change("tokenized", "Tokenized by HisTEI", $userID)
    let $newRevisionDesc := teix:update-revisionDesc($change, $teiHeader/revisionDesc[1])
    let $newContents := $teiHeader/node() except ($teiHeader/fileDesc, $teiHeader/revisionDesc)
    let $newHeader := utils:replace-content($teiHeader, ( $newFileDesc, $newContents, $newRevisionDesc ) )
    return
        utils:replace-content($tei, ( $newHeader, $tei/node() except $tei/teiHeader ))
};

declare function tok:tokenize($teiElements as element(TEI)*, $userID as xs:string?, 
                                    $tokenTypes as map(xs:integer, map(xs:string, item()))?) as element(TEI)* {
    
    let $tokenTypes := if (empty($tokenTypes)) then $tok:defaultTokenTypes else $tokenTypes
    for $tei in $teiElements
    let $trimmedTrans := tok:clean-spaces-doc($tei)
    let $splitTrans := tok:split-sub-word-elements-doc($trimmedTrans)
    let $tokenized := tok:tokenize-doc($tokenTypes, $splitTrans)
    return
        tok:update-header($tokenized, $userID)
};

declare function tok:tokenize($teiElements as element(TEI)*, $userID as xs:string?) as element(TEI)* {
    tok:tokenize($teiElements, $userID, ())
};

declare function tok:tokenize($teiElements as element(TEI)*) as element(TEI)* {
    tok:tokenize($teiElements)
};

declare function tok:tokenize-collection($teiURI as xs:anyAtomicType, $tokenizedURI as xs:anyAtomicType, $userID as xs:string?) as element() {
    let $teiPath := utils:get-dir-path($teiURI)
    let $tokenizedPath := utils:get-dir-path($tokenizedURI)
    return
        utils:element-NS("tokenizedFiles",
            (
                utils:attribute-NS("userID", $userID),
                if (exists($teiPath) and exists($tokenizedPath)) then
                    let $docs := teix:collection(utils:saxon-collection-uri($teiPath))
                    return
                    (
                        utils:attribute-NS("teiPath", $teiPath),
                        utils:attribute-NS("tokenizedPath", $tokenizedPath),
                        utils:attribute-NS("total", count($docs)),
                        for $doc in $docs
                        let $tokenizedTrans := tok:tokenize($doc/TEI[1], $userID)
                        let $newFilePath := utils:write-transformation($teiPath, $tokenizedPath, $doc, $tokenizedTrans)
                        order by $newFilePath
                        return
                            utils:element-NS("file", ( 
                                utils:element-NS("path", $newFilePath),
                                utils:element-NS("wordCount", data($tokenizedTrans//fileDesc/extent/measure[@unit eq "words"]/@quantity[1]) )
                            ))
                    )
                else
                (
                    utils:element-NS("transDir", ( utils:attribute-NS("invalid", empty($teiPath)), $teiURI )),
                    utils:element-NS("tokenizedDir", ( utils:attribute-NS("invalid", empty($tokenizedPath) ), $tokenizedURI )),
                    utils:element-NS("error", "The provided directories are invalid!" )
                )
            )
        )
};







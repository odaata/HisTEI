xquery version "3.0";

(:~
 : A set of generic XML helper functions for use in XQuery scripts in the HisTEI framework
 :)
module namespace utils="http://histei.info/xquery/utils";

import module namespace functx="http://www.functx.com" at "functx.xql";

declare namespace file="http://expath.org/ns/file";
declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace output="http://www.w3.org/2010/xslt-xquery-serialization";
declare namespace tei="http://www.tei-c.org/ns/1.0";

declare namespace decoder="java:java.net.URLDecoder";

declare variable $utils:OUTPUT_NO_INDENT := <output:serialization-parameters><output:indent value="no"/></output:serialization-parameters>;

(: File-related Functions :)

(:
    Gets the document URI and then returns the filename portion of the path for each input document-node()
:)
declare function utils:get-filenames($docs as document-node()*) as xs:string* {
    for $doc in $docs
    return
        decoder:decode(functx:substring-after-last(string(document-uri($doc)), "/"), "UTF-8")
};

(:
    Gets the document URI and then returns the base portion (without the extension) of the filename for each input document-node()
:)
declare function utils:get-file-basenames($docs as document-node()*) as xs:string* {
    let $filenames := utils:get-filenames($docs)
    for $filename in $filenames
    return
        functx:substring-before-last($filename, ".")
};

(: Checks if a path is valid and also a directory. 
    If a file is given, the path to the parent directory is returned.
    If a path is invalid or does not exist, an empty string is returned.   
    Paths are uniformly returned with a slash on the end (to make adding file names to them easier)
:)
declare function utils:get-dir-path($uri as xs:anyURI?) as xs:string? {
    if (empty($uri)) then
        ()
    else
        let $path := utils:uri-to-path($uri)
        let $path := 
            if (file:is-dir($path)) then
                $path
            else if (file:is-file($path)) then
                functx:substring-before-last($path, file:dir-separator()) 
            else
                ()
        return
            if (empty($path)) then
                ()
            else
                utils:resolve-path($path)
};

(:
    Converts a URI to a native path
    - removes file: prefix and replaces the URI / with whatever the platform separator is
    - if the URI schema is NOT file:, it returns nothing, since there's no native path
:)
declare function utils:uri-to-path($uri as xs:anyURI?) as xs:string? {
    if (empty($uri)) then
        ()
    else
        let $uriString := string($uri)
        let $path := 
            if (starts-with($uriString, "file:")) then
                if (file:dir-separator() eq "/") then
                    substring-after($uriString, "file:")
                else
                    replace(substring-after($uriString, "file:/"), "/", 
                        functx:escape-for-regex(file:dir-separator())
                    )
            else
                ()
        return
            decoder:decode($path, "UTF-8")
};

declare function utils:path-to-uri($path as xs:string?) as xs:anyURI? {
    if (empty($path) or $path eq "") then
        ()
    else
        let $path := 
            if (file:dir-separator() eq "/") then
                substring($path, 2)
            else
                replace($path, functx:escape-for-regex(file:dir-separator()), "/")
        let $encoded := 
            string-join(
                for $part in tokenize($path, "/")
                return
                    if (matches($part, "^\p{L}+:$")) then
                        $part
                    else
                        encode-for-uri($part)
                , "/"
            )
        return
            xs:anyURI(concat("file:/", $encoded))
};

declare function utils:resolve-path($dir as xs:string?, $file as xs:string?) as xs:string? {
    if (empty($dir)) then
        ()
    else
        let $dir := 
            if (ends-with($dir, file:dir-separator())) then
                $dir
            else
                concat($dir, file:dir-separator())
        return
            concat($dir, $file)
};

declare function utils:resolve-path($dir as xs:string?) as xs:string? {
    utils:resolve-path($dir, ())
};

(:
    Parse a tab-delimited file and return its rows as a collection of maps with the keys being  
        either header names taken from the first row or the ordinal position of each field
:)
declare function utils:parse-tab-file($path as xs:string, $hasHeaders as xs:boolean?) as element(row)* {
    let $hasHeaders := if (empty($hasHeaders)) then true() else $hasHeaders
    let $fieldPrefix := "f_"
    
    let $lines := file:read-text-lines($path)
    let $fieldNames := 
        for $field at $pos in tokenize($lines[1], "\t")
        return
            if ($hasHeaders) then
                let $field := normalize-space($field)
                return
                    if ($field castable as xs:QName) then 
                        $field 
                    else 
                        concat($fieldPrefix, $field)
            else
                concat($fieldPrefix, $pos)
           
    let $body := if ($hasHeaders) then subsequence($lines, 2) else $lines
    
    for $line in $body
    return
        element row {
            for $field at $pos in tokenize($line, "\t")
            let $fieldName := $fieldNames[$pos]
            let $fieldName := if (empty($fieldName) or $fieldName eq "") then concat($fieldPrefix, $pos) else $fieldName
            return
                element { $fieldName } { normalize-space($field) }
        }
};

declare function utils:parse-tab-file($path as xs:string) as element(row)* {
    utils:parse-tab-file($path, ())
};


(: Generic functions for processing XML :)

(: $type can be "anywhere", "starts", "ends", "all" nothing defaults to "anywhere" :)
declare function utils:contains-ws($string as xs:string?, $type as xs:string?) as xs:boolean {
    let $regex :=
        switch ($type)
        case "starts" return "^\s"
        case "ends" return "\s$"
        case "all" return "^\s+$"
        default return "\s"
    return
        matches($string, $regex)
};

declare function utils:contains-ws($string as xs:string?) as xs:boolean {
    utils:contains-ws($string, ())
};

declare function utils:is-empty-oxy-comment($textNode as node()?) as xs:boolean {
    (
        exists($textNode) 
        and $textNode instance of text()
        and $textNode/preceding-sibling::node()[1] instance of processing-instruction(oxy_comment_start)
        and $textNode/following-sibling::node()[1] instance of processing-instruction(oxy_comment_end)
        and normalize-space($textNode) eq ""
    )
};

declare function utils:non-empty-text-nodes($element as element()) as text()* {
    $element//text()[normalize-space() ne ""]
};

declare function utils:replace-content($element as element(), $newContent, 
                                            $newAttributes as attribute()*) as element() {
    element { node-name($element) } {
        if (exists($newAttributes)) then $newAttributes else $element/@*,
        $newContent
    }
};

declare function utils:replace-content($element as element(), $newContent) as element() {
    utils:replace-content($element, $newContent, ())
};

declare function utils:update-attributes($element as element(), $newAttributes as attribute()*) as element() {
    let $newAttrNames := for $attr in $newAttributes return local-name($attr)
    return
        element { node-name($element) } {
            $element/@* except $element/@*[local-name() = $newAttrNames], 
            $newAttributes
        }
};

declare function utils:update-content-ordered($element as element(), $fieldNames as xs:string+, 
                                                            $newElements as element()*) as element() {
    let $nodes := $element/node()
    let $fieldsMap := map:new(
        for $fieldName in $fieldNames
        return
            map:entry($fieldName, 
                for $node at $pos in $nodes
                return
                    if ($node instance of element() and local-name($node) eq $fieldName) then
                        $pos
                    else
                        ()
            )
    )
    let $startFunc := function($pos as xs:integer) as xs:integer {
        if ($pos eq 1) then 
            1 
        else 
            let $prevLocs := for $n in (1 to $pos - 1) return $fieldsMap($fieldNames[$n])[last()]
            return
                if (exists($prevLocs)) then $prevLocs[last()] + 1 else 1
    }
    let $newNodes := 
        for $fieldName at $pos in $fieldNames
        let $newElement := $newElements[local-name() eq $fieldName]
        
        let $locs := $fieldsMap($fieldName)
        let $oldElement := 
            if (empty($locs)) then
                ()
            else if (count($locs) eq 1) then
                $nodes[$locs[1]]
            else
                subsequence($nodes, $locs[1], ($locs[last()] - $locs[1]) + 1)
        
        let $updatedElement := 
            if (exists($newElement)) then 
            (
                $newElement,
                $oldElement except $oldElement[local-name() eq $fieldName]
            )
            else 
                $oldElement
        
        let $prevNodes := 
            if (empty($locs)) then
                ()
            else
                let $start := $startFunc($pos)
                return
                    subsequence($nodes, $start, $locs[1] - $start)
        return
            ( $prevNodes, $updatedElement )
    return
        element { node-name($element) } {
            $element/@*,
            $newNodes,
            subsequence($nodes, $startFunc(count($fieldNames) + 1))
        }
};




